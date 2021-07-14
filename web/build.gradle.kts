plugins {
    id("com.github.node-gradle.node") version "3.1.0"
}

node {
    nodeProjectDir.set(file("${project.projectDir}"))
    download.set(true)
}

tasks.register<Delete>("clean") {
    delete("dist")
    delete("node_modules")

}

tasks.register<com.github.gradle.node.yarn.task.YarnTask>("build") {
    dependsOn("yarn_install")
    args.set(listOf("build"))
}

tasks.register<com.github.gradle.node.yarn.task.YarnTask>("serve") {
    dependsOn("yarn_install")
    args.set(listOf("serve"))

}

tasks.register<com.github.gradle.node.yarn.task.YarnTask>("lint") {
    dependsOn("yarn_install")
    args.set(listOf("lint"))
}

tasks.register<com.github.gradle.node.yarn.task.YarnTask>("check") {
    dependsOn("lint")
}