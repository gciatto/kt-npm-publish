package io.github.gciatto.kt.node

import com.google.gson.JsonElement
import com.google.gson.JsonObject

data class PackageJson(
    var name: String? = null,
    var main: String? = null,
    var version: String? = null,
    var dependencies: MutableMap<String, String>? = null,
    var devDependencies: MutableMap<String, String>? = null,
    var peerDependencies: MutableMap<String, String>? = null,
    var homepage: String? = null,
    var bugs: Bugs? = null,
    var keywords: MutableList<String>? = null,
    var people: MutableList<People>? = null,
    var license: String? = null,
    var files: MutableList<String>? = null,
    var bin: String? = null,
    var bins: MutableMap<String, String>? = null
) {
    companion object {
        fun fromJson(element: JsonElement): PackageJson =
            PackageJson(
                name = element.getPropertyOrNull("name"),
                main = element.getPropertyOrNull("main"),
                version = element.getPropertyOrNull("version"),
                homepage = element.getPropertyOrNull("homepage"),
                bugs = element.getOrNull("bugs")?.let { Bugs.fromJson(it) },
                license = element.getPropertyOrNull("license"),
                bin = element.getOrNull("bin")?.let { if (it.isJsonPrimitive) it.asString else null },
                bins = element.getOrNull("bin")?.asPropertyMapOrNull(),
                dependencies = element.getOrNull("dependencies")?.asPropertyMapOrNull(),
                devDependencies = element.getOrNull("devDependencies")?.asPropertyMapOrNull(),
                peerDependencies = element.getOrNull("peerDependencies")?.asPropertyMapOrNull(),
                keywords = element.getOrNull("keywords")?.asPropertyListOrNull(),
                files = element.getOrNull("files")?.asPropertyListOrNull(),
                people = element.getOrNull("people")?.asListOrNull()
                    ?.map { People.fromJson(it) }?.toMutableList()
            )
    }

    fun toJson(): JsonObject {
        return JsonObject().also {
            it.addSimpleProperties()
            it.addComplexProperties()
        }
    }

    private fun JsonObject.addSimpleProperties() {
        name?.let { addProperty("name", it) }
        main?.let { addProperty("main", it) }
        version?.let { addProperty("version", it) }
        homepage?.let { addProperty("homepage", it) }
        license?.let { addProperty("license", it) }
        bugs?.let { add("bugs", it.toJson()) }
        bin?.let { addProperty("bin", it) }
    }

    private fun JsonObject.addComplexProperties() {
        keywords?.let { add("keywords", jsonArray(it.map(String::toJson))) }
        files?.let { add("files", jsonArray(it.map(String::toJson))) }
        people?.let { add("people", jsonArray(it.map(People::toJson))) }
        dependencies?.let { add("dependencies", jsonObject(it)) }
        devDependencies?.let { add("devDependencies", jsonObject(it)) }
        peerDependencies?.let { add("peerDependencies", jsonObject(it)) }
        bins?.let { add("bin", jsonObject(it)) }
    }
}
