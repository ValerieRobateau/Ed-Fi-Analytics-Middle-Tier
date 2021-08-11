package _self.release.buildTypes

import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.dotnetPublish
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.powerShell


object BuildAndTestReleaseCandidate : BuildType({
    templates(_self.templates.BuildAndTestNetCoreApplication)
    name = "Build and Test Release Candidate"

    params {
        param("BranchToBuild", "master")
    }

    triggers {
        vcs {
            id = "vcsTrigger"
            enabled = false
            branchFilter = "+:refs/heads/master"
        }
    }
})