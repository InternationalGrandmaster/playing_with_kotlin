import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import com.fasterxml.jackson.dataformat.xml.*
import com.fasterxml.jackson.module.kotlin.*

@JacksonXmlRootElement(localName = "Product")
data class Product(
    @JacksonXmlProperty(localName = "id")
    val id: Long,

    @JacksonXmlProperty(localName = "name")
    val name: String,

    @JacksonXmlProperty(localName = "price")
    val price: Double
)