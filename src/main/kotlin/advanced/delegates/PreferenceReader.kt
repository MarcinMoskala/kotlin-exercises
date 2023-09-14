package advanced.delegates

// TODO: Implement PreferenceReaderBinder and uncomment the rest

// object Preferences : PreferenceReaderBinder("UserPreferences") {
//     var token by preferenceField("token")
//     var id by preferenceField("id")
// }

class PreferenceReader(private val name: String) {
    fun read(preference: String): String? = FakePreferences.preferences[name to preference]

    fun write(preference: String, value: String?) {
        FakePreferences.preferences += (name to preference) to value
    }
}

object FakePreferences {
    // Maps preference file name and preference field name to actual value
    var preferences: Map<Pair<String, String>, String?> = mapOf()
}

//class PreferencesTests {
//    @Test
//    fun `We can store or read values`() {
//        // Store
//        Preferences.token = "Test"
//        Preferences.id = "Test"
//        // Read
//        Preferences.token
//        Preferences.id
//    }
//
//    @Test
//    fun `Retrieved value is the same as the one what was saved`() {
//        val test1 = "Test"
//        val test2 = "Test2"
//
//        Preferences.token = test1
//        Preferences.id = test1
//        assertEquals(test1, Preferences.token)
//        assertEquals(test1, Preferences.id)
//
//        Preferences.token = test2
//        assertEquals(test2, Preferences.token)
//        assertEquals(test1, Preferences.id)
//
//        Preferences.id = test2
//        // Read
//        assertEquals(test2, Preferences.token)
//    }
//
//    @Test
//    fun `Storage and reading both use design.FakePreferences`() {
//        val test1 = "Test"
//        val test2 = "Test2"
//
//        Preferences.token = test1
//        assertEquals(test1, design.FakePreferences.preferences["UserPreferences" to "token"])
//
//        design.FakePreferences.preferences += ("UserPreferences" to "token") to test2
//        assertEquals(test2, Preferences.token)
//    }
//}
