import jetbrains.buildServer.configs.kotlin.v2019_2.*

version = "2021.1"

project {

    template(BuildAndTestNetCoreApplication)

    params {
        param("SolutionFile", """src\%SolutionName%.sln""")
        param("SolutionName", "EdFi.AnalyticsMiddleTier")
        param("TestLibraries", """src\%SolutionName%.Tests\bin\Release\netcoreapp3.0\%SolutionName%.Tests.dll""")
        password("EdFiBuildAgentAccessToken", "zxxb4df31ef99eb9b3e58e4ff7a346e6c293a63de66adf1c09ce8448aa20aae900965a234002ad4183f775d03cbe80d301b")
        param("PreReleaseVersionNumber", "%SemanticVersionNumber%-pre%build.counter%")
        param("SemanticVersionNumber", "2.0.0")
    }

    subProject(PreRelease)
    subProject(Release)
}

