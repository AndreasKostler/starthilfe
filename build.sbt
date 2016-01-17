uniform.project("starthilfe", "au.com.cba.omnia.starthilfe")

val omnitoolVersion    = "1.12.0-20160104235530-f3eb090"
val rengineVersion     = "2.1.0"

uniformDependencySettings
strictDependencySettings

libraryDependencies :=
  depend.testing() ++ 
  depend.omnia("omnitool-core", omnitoolVersion) ++
  Seq(
    "org.specs2"               %% "specs2-core"   % depend.versions.specs,
    "au.com.cba.omnia"         %% "omnitool-core" % omnitoolVersion % "test" classifier "tests",
    "org.rosuda.REngine"       % "REngine"        % rengineVersion
  )

// R environment variables
// TODO: Reasonable error message if R_HOME not set
val rHome: String = sys.env.get("R_HOME").get

unmanagedJars in Compile <++= baseDirectory map { base =>
  val baseDirectories = base +++ file(s"$rHome/library/rJava/jri")
  val customJars = (baseDirectories ** "*.jar")
  customJars.classpath
}

val libPath = s"${rHome}/library/rJava/jri:" + System.getProperty("Djava.library.path")

javaOptions in Test += s"-Djava.library.path=$libPath"

fork in Test := true

updateOptions                     := updateOptions.value.withCachedResolution(true)
publishArtifact           in Test := true
parallelExecution         in Test := false

uniform.docSettings("https://github.com/CommBank/starthilfe")
uniform.ghsettings

