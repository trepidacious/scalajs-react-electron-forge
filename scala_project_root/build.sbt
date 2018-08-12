import sbtcrossproject.CrossPlugin.autoImport.crossProject

name := "scalajs-react-electron-forge root"

version in ThisBuild := "0.0.1"

organization in ThisBuild := "org.rebeam"

scalaVersion in ThisBuild := "2.12.6"

// crossScalaVersions in ThisBuild := Seq("2.11.12", "2.12.6")

scalacOptions in ThisBuild ++= Seq(
  "-feature",
  "-deprecation",
  "-encoding", "UTF-8",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint"
)

val scalajsReactVersion = "1.2.3"

val scalaJsSrcDir = file("../scalajs_src")

lazy val root = project.in(file(".")).
  aggregate(scalajsReactElectronForgeJS, scalajsReactElectronForgeJVM).
  settings(
    publish := {},
    publishLocal := {}
  )

lazy val scalajsReactElectronForge = crossProject(JSPlatform, JVMPlatform).in(file(".")).
  //Settings for all projects
  settings(
    name := "scalajs-react-electron-forge",
  ).jsSettings(
    //Scalajs dependencies that are used on the client only
    libraryDependencies ++= Seq(
      "com.github.japgolly.scalajs-react" %%% "core" % scalajsReactVersion,
      "com.github.japgolly.scalajs-react" %%% "extra" % scalajsReactVersion
    ),
    
    //Output scalajs and js dependencies to source folder for electron project
    crossTarget in (Compile, fullOptJS) := scalaJsSrcDir,
    crossTarget in (Compile, fastOptJS) := scalaJsSrcDir,
    crossTarget in (Compile, packageJSDependencies) := scalaJsSrcDir,

    // This is an application with a main method
    scalaJSUseMainModuleInitializer := true
  )

lazy val scalajsReactElectronForgeJVM = scalajsReactElectronForge.jvm
lazy val scalajsReactElectronForgeJS = scalajsReactElectronForge.js

