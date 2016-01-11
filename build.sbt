uniform.project("starthilfe", "au.com.cba.omnia.starthilfe")

val omnitoolVersion    = "1.12.0-20151021050758-700b9d0"
val thermometerVersion = "1.3.0-20151122230202-55282c8"
val ebenezerVersion    = "0.21.2-20160104234501-8e2043a"

uniformDependencySettings
uniformThriftSettings
strictDependencySettings

libraryDependencies :=
  depend.testing() ++ 
  depend.omnia("ebenezer", ebenezerVersion, "test") ++
  depend.omnia("omnitool-core", omnitoolVersion) ++
  depend.omnia("thermometer-hive", thermometerVersion, "test") ++
  Seq(
    "org.specs2"               %% "specs2-core"   % depend.versions.specs,
    "au.com.cba.omnia"         %% "omnitool-core" % omnitoolVersion % "test" classifier "tests"
  )

updateOptions                     := updateOptions.value.withCachedResolution(true)
publishArtifact           in Test := true
parallelExecution         in Test := false
scroogeThriftSourceFolder in Test <<= (sourceDirectory) { _ / "test" / "thrift" }

uniform.docSettings("https://github.com/CommBank/starthilfe")
uniform.ghsettings

