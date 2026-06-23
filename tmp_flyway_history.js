const { Client } = require('C:/Users/user/AppData/Local/Temp/pgflyway/node_modules/pg');
const connStr = process.env.DATABASE_PUBLIC_URL || process.env.DATABASE_URL;
const client = new Client({ connectionString: connStr, ssl: { rejectUnauthorized: false } });
client.connect()
  .then(() => client.query(`SELECT installed_rank, version, description, checksum, success, installed_on FROM flyway_schema_history ORDER BY installed_rank`))
  .then(r => {
    console.table(r.rows);
    return client.end();
  })
  .catch(e => {
    console.error('ERR: ' + e.message);
    process.exit(1);
  });
