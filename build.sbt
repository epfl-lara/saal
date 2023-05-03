ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.2.0"

def ghProject(repo: String, version: String) = RootProject(uri(s"${repo}#${version}"))

lazy val stainless = ghProject("https://github.com/epfl-lara/stainless.git", "98bbc622e78dca763ea9918e6e02f31806ec2316")

lazy val root = (project in file("."))
  .settings(
    name := "saal"
  ).dependsOn(stainless)
