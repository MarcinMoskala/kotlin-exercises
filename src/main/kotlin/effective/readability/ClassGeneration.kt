package readable

import org.junit.Test
import readable.ClassModel
import readable.GenerateDtoOptions
import readable.GenerateRequest
import readable.ParameterModel
import kotlin.test.assertEquals

class DtoGeneratorService {

    fun generateDto(model: ClassModel,     options: GenerateDtoOptions):            String {
        val newName: String=options.newName.takeUnless { it.isNullOrBlank() }
            ?: model.name + (options.suffix

                .takeUnless { it.isNullOrBlank() } ?: "Json")
        val paramsText=model.params
            .takeIf { it.isNotEmpty() }
            ?.joinToString(
                separator=",\n   ", prefix="\n   ",
                postfix="\n", transform={ it: ParameterModel -> "${it.visibilityModifier} ${it.name}: ${it.type}" }
            )
            .orEmpty()

        val paramTransformationsText      =model.params
            .takeIf { it.isNotEmpty() }
            ?.joinToString(separator=",\n   ",
                prefix= "\n   ",
                postfix="\n",
                transform={ it: ParameterModel ->    "${it.name}=${it.name}" }
            )
            .orEmpty()
        val paramTransformationsText1=model.params.takeIf { it.isNotEmpty() }
            ?.joinToString(
                separator=",\n   ",
                prefix="\n   ",
                postfix="\n",
                transform={ it: ParameterModel -> "${it.name}=${it.name}" }
            )
            .orEmpty()
        return """
            |class $newName($paramsText)
            |
            |${"fun ${model.name}.to${newName.capitalize()}()=$newName($paramTransformationsText1)"}
            |
            |${"fun $newName.to${model.name.capitalize()}()=${model.name}($paramTransformationsText1)"}""".trimMargin()
    }

    fun generateGroovyBuilder(model: ClassModel): String {
        val builderName: String=model.name + "Build"

        val fields=model.params
            .joinToString(
                separator="\n   ", transform = { "${
                    when (it.type) {
                        "Int" -> "int"
                        "Int?" -> "Integer"
                        "Long" -> "long"
                        "Long?" -> "Long"
                        "Float" -> "float"
                        "Float?" -> "Float"
                        "Char" -> "char"
                        "Char?" -> "Char"
                        else -> when {
                            it.type.endsWith(">") -> genericTypeToJava(it.type)
                            else -> it.type
                        }
                    }
                } ${it.name}" }
            )

        return """
            |@Builder(builderStrategy = SimpleStrategy, prefix = "with")
            |class $builderName {
            |   $fields
            |   
            |   static $builderName a${builderName.capitalize()}() {
            |       return new $builderName()
            |   }
            |   
            |   ${model.name} build() {
            |       return new ${model.name}(${model.params.joinToString { it.name }})
            |   }
            |}""".trimMargin()
    }

    fun generateGroovyObjectAssertion(  model:   ClassModel): String {
        val assertionName: String = model.name + "Assertion"
        val fieldName: String = model.name.decapitalize()

        fun     generateAssertionFunction(parameter: ParameterModel): String = """
            |$assertionName has${parameter.name.capitalize()}(${
            when (parameter.type) {
                "Int" -> "int"
                "Int?" -> "Integer"
                "Long" -> "long"
                "Long?" -> "Long"
                "Float" -> "float"
                "Float?" -> "Float"
                "Char" -> "char"
                "Char?" -> "Char"
                else -> when {
                    parameter.type.endsWith(">") -> genericTypeToJava(parameter.type)
                    else -> parameter.type
                }
            }
        } ${parameter.name}) {
            |    assert $fieldName.${parameter.name} == ${parameter.name}
            |    return this
            |}
        """.trimMargin()

        val assertionFunctions = model.params
            .filterNot { it.name.startsWith("`") }
            .takeIf { it.isNotEmpty() }
            ?.joinToString(
                separator = "\n\n",
                transform = ::generateAssertionFunction
            )
            ?.replace("\n", "\n${"   "}")
            .orEmpty()

        return """
            |class $assertionName {
            |   private ${model.name} $fieldName
            |   
            |   $assertionName(${model.name} $fieldName) {
            |       this.$fieldName = $fieldName
            |   }
            |   
            |   static $assertionName assertThat(${model.name} $fieldName) {
            |       return $assertionName($fieldName)
            |   }
            |   
            |   $assertionFunctions
            |}""".trimMargin()
    }

    fun generateTypeScriptObject(model: ClassModel): String {
        val assertionFunctions = model.params
            .takeIf { it.isNotEmpty() }
            ?.joinToString(
                separator = ",\n   ",
                prefix = "\n   ",
                postfix = "\n",
                transform = { p: ParameterModel -> "${p.name}: ${typeToTypeScript(p.type)}" }
            )
            .orEmpty()

        return """
            |type ${model.name} = {$assertionFunctions}""".trimMargin()
    }

    private fun typeToTypeScript(type: String): String = when (type) {
        "Int", "Long", "Float" -> "number"
        "Int?", "Long?", "Float?" -> "number"
        "Char", "String" -> "string"
        "Char?", "String?" -> "string"
        else -> when {
            type.startsWith("List<") || type.startsWith("Set<") || type.startsWith("Array<") -> {
                val (_, paramsStr) = "(.*?)<(.*?)>[?]?\$".toRegex()
                    .find(type)
                    ?.groupValues
                    ?.let { it[1] to it[2] }!!
                typeToTypeScript(paramsStr) + "[]"
            }
            else -> type
        }
    }

    private fun genericTypeToJava(type: String): String {
        val (baseName, paramsStr) = "(.*?)<(.*?)>[?]?\$".toRegex()
            .find(type)
            ?.groupValues
            ?.let { it[1] to it[2] } ?: return type

        val javaParams = paramsStr
            .split(",")
            .map { it.trim() }
            .joinToString(separator = ",") {
                when (it) {
                    "Int", "Int?" -> "Integer"
                    "Long", "Long?" -> "Long"
                    "Float", "Float?" -> "Float"
                    "Char", "Char?" -> "Char"
                    else -> it
                }
            }

        return "$baseName<$javaParams>"
    }
}

data class GenerateRequest(
    val code: String,
    val options: GenerateDtoOptions,
)

data class GenerateDtoOptions(
    val newName: String? = null,
    val suffix: String? = null
)

data class ClassModel(
    val name: String,
    val params: List<ParameterModel> = emptyList()
)

data class ParameterModel(
    val readOnly: Boolean?,
    val name: String,
    val type: String
) {
    val visibilityModifier = when (readOnly) {
        true -> "val"
        false -> "var"
        null -> ""
    }
}

internal open class DtoGeneratorTests {

    protected val service = DtoGeneratorService()
}

internal class GenerateDtoTests : DtoGeneratorTests() {

    @Test
    fun `should make default suffix`() {
        val model = ClassModel("Name", emptyList())
        val expected = """
            |class NameJson()
            |
            |fun Name.toNameJson() = NameJson()
            |
            |fun NameJson.toName() = Name()
        """.trimMargin()
        assertEquals(expected, service.generateDto(model, GenerateDtoOptions()))
    }

    @Test
    fun `should respect new name`() {
        val model = ClassModel("Name", emptyList())
        val expected = """
            |class NewName()
            |
            |fun Name.toNewName() = NewName()
            |
            |fun NewName.toName() = Name()
        """.trimMargin()
        assertEquals(expected, service.generateDto(model, GenerateDtoOptions(newName = "NewName")))
    }

    @Test
    fun `should respect suffix`() {
        val model = ClassModel("Name", emptyList())
        val expected = """
            |class NameEntity()
            |
            |fun Name.toNameEntity() = NameEntity()
            |
            |fun NameEntity.toName() = Name()
        """.trimMargin()
        assertEquals(expected, service.generateDto(model, GenerateDtoOptions(suffix = "Entity")))
    }

    @Test
    fun `should generate for simple class`() {
        val model = ClassModel("Name", listOf(ParameterModel(false, "name", "String")))
        val expected = """
            |class NameEntity(
            |   var name: String
            |)
            |
            |fun Name.toNameEntity() = NameEntity(
            |   name = name
            |)
            |
            |fun NameEntity.toName() = Name(
            |   name = name
            |)
        """.trimMargin()
        assertEquals(expected, service.generateDto(model, GenerateDtoOptions(suffix = "Entity")))
    }

    @Test
    fun `should generate for complex class`() {
        val model = ClassModel(
            "User",
            listOf(
                ParameterModel(true, "name", "String"),
                ParameterModel(true, "surname", "String"),
                ParameterModel(true, "age", "Int"),
                ParameterModel(true, "`something strange`", "String"),
                ParameterModel(true, "ids", "List<Int>")
            )
        )
        val expected = """
            |class NewName(
            |   val name: String,
            |   val surname: String,
            |   val age: Int,
            |   val `something strange`: String,
            |   val ids: List<Int>
            |)
            |
            |fun User.toNewName() = NewName(
            |   name = name,
            |   surname = surname,
            |   age = age,
            |   `something strange` = `something strange`,
            |   ids = ids
            |)
            |
            |fun NewName.toUser() = User(
            |   name = name,
            |   surname = surname,
            |   age = age,
            |   `something strange` = `something strange`,
            |   ids = ids
            |)
        """.trimMargin()
        assertEquals(expected, service.generateDto(model, GenerateDtoOptions(newName = "NewName")))
    }
}

internal class GenerateGroovyAssertionTests : DtoGeneratorTests() {

    @Test
    fun `should generate for simple class`() {
        val model = ClassModel("Name", listOf(ParameterModel(false, "name", "String")))
        val expected = """
            |class NameAssertion {
            |   private Name name
            |   
            |   NameAssertion(Name name) {
            |       this.name = name
            |   }
            |   
            |   static NameAssertion assertThat(Name name) {
            |       return NameAssertion(name)
            |   }
            |   
            |   NameAssertion hasName(String name) {
            |       assert name.name == name
            |       return this
            |   }
            |}
        """.trimMargin()
        assertEquals(expected, service.generateGroovyObjectAssertion(model))
    }

    @Test
    fun `should generate for complex class`() {
        val model = ClassModel(
            "User",
            listOf(
                ParameterModel(true, "name", "String"),
                ParameterModel(true, "surname", "String"),
                ParameterModel(true, "age", "Int"),
                ParameterModel(true, "`something strange`", "String"),
                ParameterModel(true, "ids", "List<Int>")
                )
        )
        val expected = """
            |class UserAssertion {
            |   private User user
            |   
            |   UserAssertion(User user) {
            |       this.user = user
            |   }
            |   
            |   static UserAssertion assertThat(User user) {
            |       return UserAssertion(user)
            |   }
            |   
            |   UserAssertion hasName(String name) {
            |       assert user.name == name
            |       return this
            |   }
            |   
            |   UserAssertion hasSurname(String surname) {
            |       assert user.surname == surname
            |       return this
            |   }
            |   
            |   UserAssertion hasAge(int age) {
            |       assert user.age == age
            |       return this
            |   }
            |   
            |   UserAssertion hasIds(List<Integer> ids) {
            |       assert user.ids == ids
            |       return this
            |   }
            |}
        """.trimMargin()
        assertEquals(expected, service.generateGroovyObjectAssertion(model))
    }
}

internal class GenerateGroovyBuildTests : DtoGeneratorTests() {

    @Test
    fun `should generate for simple class`() {
        val model = ClassModel("Name", listOf(ParameterModel(false, "name", "String")))
        val expected = """
            |@Builder(builderStrategy = SimpleStrategy, prefix = "with")
            |class NameBuild {
            |   String name
            |   
            |   static NameBuild aNameBuild() {
            |       return new NameBuild()
            |   }
            |   
            |   Name build() {
            |       return new Name(name)
            |   }
            |}
        """.trimMargin()
        assertEquals(expected, service.generateGroovyBuilder(model))
    }

    @Test
    fun `should generate for complex class`() {
        val model = ClassModel(
            "User",
            listOf(
                ParameterModel(true, "name", "String"),
                ParameterModel(true, "surname", "String"),
                ParameterModel(true, "age", "Int"),
                ParameterModel(true, "ids", "List<Int>")
                )
        )
        val expected = """
            |@Builder(builderStrategy = SimpleStrategy, prefix = "with")
            |class UserBuild {
            |   String name
            |   String surname
            |   int age
            |   List<Integer> ids
            |   
            |   static UserBuild aUserBuild() {
            |       return new UserBuild()
            |   }
            |   
            |   User build() {
            |       return new User(name, surname, age, ids)
            |   }
            |}
        """.trimMargin()
        assertEquals(expected, service.generateGroovyBuilder(model))
    }
}

internal class GenerateTypeScriptTests : DtoGeneratorTests() {

    @Test
    fun `should generate for empty`() {
        val model = ClassModel("Name", emptyList())
        val expected = """
            |type Name = {}
        """.trimMargin()
        assertEquals(expected, service.generateTypeScriptObject(model))
    }

    @Test
    fun `should generate for simple class`() {
        val model = ClassModel("Name", listOf(ParameterModel(false, "name", "String")))
        val expected = """
            |type Name = {
            |   name: string
            |}
        """.trimMargin()
        assertEquals(expected, service.generateTypeScriptObject(model))
    }

    @Test
    fun `should generate for complex class`() {
        val model = ClassModel(
            "User",
            listOf(
                ParameterModel(true, "name", "String"),
                ParameterModel(true, "surname", "String"),
                ParameterModel(true, "age", "Int"),
                ParameterModel(true, "`something strange`", "String"),
                ParameterModel(true, "ids", "List<Int>")
            )
        )
        val expected = """
            |type User = {
            |   name: string,
            |   surname: string,
            |   age: number,
            |   `something strange`: string,
            |   ids: number[]
            |}
        """.trimMargin()
        assertEquals(expected, service.generateTypeScriptObject(model))
    }
}
