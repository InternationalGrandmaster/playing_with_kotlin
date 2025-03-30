import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.fasterxml.jackson.dataformat.xml.*
import com.fasterxml.jackson.module.kotlin.*
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.util.Properties


fun main() {
    val xmlMapper = XmlMapper().registerKotlinModule()
        .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)

    /*I have already set environment variables/strings in intellij idea configurations
    * for db connection url, db user and db password for that user*/

    // Get environment variables
    val dbUrl = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5432/postgres"
    val dbUser = System.getenv("DB_USER") ?: "my_username"
    val dbPassword = System.getenv("DB_PASSWORD") ?: "my_password"


    try {
        // Read the XML file
        val xmlContent = File("src/main/resources/products.xml").readText()

        // Parse XML to Kotlin objects
        val catalog = xmlMapper.readValue(xmlContent, ProductCatalog::class.java)

        // Print the parsed data
        println("Parsed Products:")
        catalog.products.forEach { product ->
            println("ID: ${product.id}")
            println("Name: ${product.name}")
            println("Price: ${product.price}")
            println("---")
        }

        /*In psql I have already created this empty table
        * CREATE TABLE IF NOT EXISTS products (
            id BIGINT PRIMARY KEY,
            name VARCHAR(255) NOT NULL,
            price DECIMAL(10, 2) NOT NULL
        );
        * */

        // Load the driver
        Class.forName("org.postgresql.Driver")

        // Create connection properties
        val props = Properties().apply {
            setProperty("user", dbUser)
            setProperty("password", dbPassword)
        }


        // Connect to database
        DriverManager.getConnection(dbUrl, props).use { conn ->

            println("Successfully connected to database")
            conn.prepareStatement("""
                INSERT INTO products (id, name, price)
                VALUES (?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET
                    name = EXCLUDED.name,
                    price = EXCLUDED.price
            """.trimIndent()).use { stmt ->
                catalog.products.forEach { product ->
                    stmt.setLong(1, product.id)
                    stmt.setString(2, product.name)
                    stmt.setDouble(3, product.price)
                    stmt.addBatch()
                }
                stmt.executeBatch()
            }
        }
        println("\nSuccessfully saved ${catalog.products.size} products to database")
    } catch (e: Exception) {
        println("Error: ${e.message}")
        if (e.message?.contains("No suitable driver") == true) {
            println("Please add the PostgreSQL JDBC driver to your Maven dependencies")
        }
    }
}