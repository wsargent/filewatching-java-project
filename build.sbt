import Dependencies._

val disableDocs = Seq[Setting[_]](
  sources in (Compile, doc) := Seq.empty,
  publishArtifact in (Compile, packageDoc) := false
)

lazy val root = (project in file("."))
  .settings(disableDocs)
  .settings(
    inThisBuild(List(
      organization := "com.example",
      crossPaths := false,
      autoScalaLibrary := false,
      fork in run := true,
      // javaOptions in run ++= Seq("-Djava.security.debug=all"),
      javaOptions in run ++= Seq(
        "-Djava.security.properties=security/macos.java.security",
        "-Dorg.cloudfoundry.security.keymanager.enabled=true",
        "-Dorg.cloudfoundry.security.trustmanager.enabled=true"
      ),
      resolvers += "Spring Release Repository" at "http://repo.spring.io/libs-release/",
      libraryDependencies += "org.cloudfoundry" % "java-buildpack-container-security-provider" % "1.16.0.RELEASE",
      libraryDependencies += "com.tersesystems.securitybuilder" % "securitybuilder" % "1.0.0",
      testOptions in Test := Seq(Tests.Argument(TestFrameworks.JUnit, "-a", "-v")),
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "filewatching-java-project",
    libraryDependencies += junitInterface % Test
  ).enablePlugins(JavaAppPackaging)

