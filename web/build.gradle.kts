import com.moowork.gradle.node.yarn.YarnTask
plugins {
    id("com.github.node-gradle.node") version "2.2.4"
}

node {
    nodeModulesDir = file("${project.projectDir}")
    download = true
}

tasks.register<Delete>("clean") {
    delete("dist")
    delete("node_modules")

}

tasks.register<YarnTask>("build") {
    dependsOn("yarn_install")
    args = listOf("build")
}

tasks.register<YarnTask>("serve") {
    dependsOn("yarn_install")
    args = listOf("serve")

}

tasks.register<YarnTask>("lint") {
    dependsOn("yarn_install")
    args = listOf("lint")
}

tasks.register<YarnTask>("check") {
    dependsOn("lint")
}