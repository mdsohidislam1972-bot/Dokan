const { initializeTestEnvironment, assertFails, assertSucceeds } = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const DATABASE_ID = "ai-studio-android-81feb082-ff21-4129-bbb9-e9b3c7ba7f41";
const OWNER_UID = "owner_user_1";
const STAFF_UID = "staff_user_2";
const STRANGER_UID = "stranger_user_3";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

let testEnv;

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
      databaseId: DATABASE_ID
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read stores or products", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("stores").get());
  await assertFails(unauthDb.collection("stores").doc("store_1").collection("products").get());
});

test("Owner: can create their store and read it", async () => {
  const ownerDb = testEnv.authenticatedContext(OWNER_UID).firestore();
  const storeRef = ownerDb.collection("stores").doc("store_1");
  await assertSucceeds(storeRef.set({
    name: "My Hardware Dokan",
    ownerId: OWNER_UID,
    phone: "01711111111",
    email: "owner@dokan.com"
  }));
  await assertSucceeds(storeRef.get());
});

test("Stranger: cannot read another owner's store", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("stores").doc("store_1").set({
      name: "Private Dokan",
      ownerId: OWNER_UID
    });
  });

  const strangerDb = testEnv.authenticatedContext(STRANGER_UID).firestore();
  await assertFails(strangerDb.collection("stores").doc("store_1").get());
});

test("Approved staff: can read products of the store", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("stores").doc("store_1").set({
      name: "Dokan Store",
      ownerId: OWNER_UID
    });
    await context.firestore().collection("stores").doc("store_1").collection("staff").doc(STAFF_UID).set({
      name: "Karim Staff",
      role: "Salesperson",
      isActive: true,
      permissions: ["create_sale", "manage_stock"]
    });
    await context.firestore().collection("stores").doc("store_1").collection("products").doc("prod_1").set({
      name: "Steel Nail",
      sellPrice: 120,
      stockQty: 50
    });
  });

  const staffDb = testEnv.authenticatedContext(STAFF_UID).firestore();
  await assertSucceeds(staffDb.collection("stores").doc("store_1").collection("products").doc("prod_1").get());
});

test("Inactive staff: cannot read products", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("stores").doc("store_1").set({
      name: "Dokan Store",
      ownerId: OWNER_UID
    });
    await context.firestore().collection("stores").doc("store_1").collection("staff").doc(STAFF_UID).set({
      name: "Fired Staff",
      role: "Salesperson",
      isActive: false,
      permissions: ["manage_stock"]
    });
    await context.firestore().collection("stores").doc("store_1").collection("products").doc("prod_1").set({
      name: "Steel Nail",
      sellPrice: 120
    });
  });

  const staffDb = testEnv.authenticatedContext(STAFF_UID).firestore();
  await assertFails(staffDb.collection("stores").doc("store_1").collection("products").doc("prod_1").get());
});
