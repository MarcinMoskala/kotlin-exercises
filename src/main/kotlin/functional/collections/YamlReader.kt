package collections

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.File

class YamlReader {
    private val mapper = ObjectMapper(YAMLFactory()).apply {
        registerModule(KotlinModule())
        configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }

    fun <T : Any> readYaml(file: File, clazz: Class<T>): T =
        mapper.readValue(file, clazz)

    inline fun <reified T : Any> readYaml(file: File): T =
        readYaml(file, T::class.java)

    fun <T : Any> writeYaml(file: File, value: T) =
        mapper.writeValue(file, value)
}
