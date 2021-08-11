package _self.preRelease.buildTypes

import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.dotnetPublish
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.powerShell

object BuildAndTestPullRequest : BuildType({
    templates(_self.templates.BuildAndTestNetCoreApplication)
    name = "Build and Test Pull Request"

    params {
        param("DotNetBuildConfiguration", "debug")
        param("PreReleaseVersionNumber", "%SemanticVersionNumber%-pre-%build.counter%")
        param("SemanticVersionNumber", "1.0.0")
    }

    triggers {
        vcs {
            id = "vcsTrigger"
            enabled = false
            branchFilter = "+:refs/pull/*/head"
        }
    }
    
    disableSettings("RUNNER_135")
})


