@file:Suppress("UNCHECKED_CAST")
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.extra

fun <A> ExtraPropertiesExtension.func(name: String, block: (A) -> Unit) { this[name] = block }
fun <A, B> ExtraPropertiesExtension.func(name: String, block: (A, B) -> Unit) { this[name] = block }
fun <A, B, C> ExtraPropertiesExtension.func(name: String, block: (A, B, C) -> Unit) { this[name] = block }
fun <A, B, C, D> ExtraPropertiesExtension.func(name: String, block: (A, B, C, D) -> Unit) { this[name] = block }
fun <A> ExtraPropertiesExtension.func(name: String, arg1: A) { (this[name] as (A) -> Unit).invoke(arg1) }
fun <A, B> ExtraPropertiesExtension.func(name: String, arg1: A, arg2: B) { (this[name] as (A, B) -> Unit).invoke(arg1, arg2) }
fun <A, B, C> ExtraPropertiesExtension.func(name: String, arg1: A, arg2: B, arg3: C) { (this[name] as (A, B, C) -> Unit).invoke(arg1, arg2, arg3) }
fun <A, B, C, D> ExtraPropertiesExtension.func(name: String, arg1: A, arg2: B, arg3: C, arg4: D) { (this[name] as (A, B, C, D) -> Unit).invoke(arg1, arg2, arg3, arg4) }
fun <A> ExtensionAware.func(name: String, arg1: A) = extra.func(name, arg1)
fun <A, B> ExtensionAware.func(name: String, arg1: A, arg2: B) = extra.func(name, arg1, arg2)
fun <A, B, C> ExtensionAware.func(name: String, arg1: A, arg2: B, arg3: C) = extra.func(name, arg1, arg2, arg3)
fun <A, B, C, D> ExtensionAware.func(name: String, arg1: A, arg2: B, arg3: C, arg4: D) = extra.func(name, arg1, arg2, arg3, arg4)

fun DependencyHandlerScope.setupNMS(ver: String) = func("setupNMS", ver)
fun Project.setupJava(ver: Int) = func("setupJava", this, ver)
