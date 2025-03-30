import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root
import org.simpleframework.xml.Serializer
import org.simpleframework.xml.core.Persister
import java.io.File
import java.sql.DriverManager
import java.sql.ResultSet
import java.util.*

@Root(name = "employee")
data class Employee(
    @field:Element(name = "id") var id: Int = 0,
    @field:Element(name = "name") var name: String = "",
    @field:Element(name = "email", required = false) var email: String? = null,
    @field:Element(name = "salary", required = false) var salary: Double? = null
)

@Root(name = "employees")
data class Employees(
    @field:ElementList(inline = true, entry = "employee") var employees: List<Employee> = emptyList()
)

fun main() {

    // Get environment variables
    val dbUrl = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5432/postgres"
    val dbUser = System.getenv("DB_USER") ?: "my_username"
    val dbPassword = System.getenv("DB_PASSWORD") ?: "my_password"

    // Database connection properties
    val props = Properties().apply {
        setProperty("user", dbUser)
        setProperty("password", dbPassword)
        setProperty("ssl", "false")
    }

    // Connect to database and query data
    val employees = mutableListOf<Employee>()

    DriverManager.getConnection(dbUrl, props).use { connection ->
        println("Connected to PostgreSQL database!")

        connection.createStatement().use { statement ->
            // Execute query - replace with your actual table name
            val resultSet = statement.executeQuery("SELECT id, name, email, salary FROM employees LIMIT 10")

            // Process results
            while (resultSet.next()) {
                employees.add(Employee(
                    id = resultSet.getInt("id"),
                    name = resultSet.getString("name"),
                    email = resultSet.getStringOrNull("email"),
                    salary = resultSet.getDoubleOrNull("salary")
                ))
            }
        }
    }

    // Print to stdout
    println("Retrieved ${employees.size} employees:")
    employees.forEach { emp ->
        println("ID: ${emp.id}, Name: ${emp.name}, Email: ${emp.email ?: "N/A"}, Salary: ${emp.salary ?: "N/A"}")
    }

    // Create XML file
    val xmlFile = File("src/main/resources/employees.xml")
    val serializer: Serializer = Persister()

    serializer.write(Employees(employees), xmlFile)
    println("\nData written to ${xmlFile.absolutePath}")
}

// Helper extension functions for ResultSet
fun ResultSet.getStringOrNull(column: String): String? {
    return try {
        getString(column)
    } catch (e: Exception) {
        null
    }
}

fun ResultSet.getDoubleOrNull(column: String): Double? {
    return try {
        getDouble(column)
    } catch (e: Exception) {
        null
    }
}