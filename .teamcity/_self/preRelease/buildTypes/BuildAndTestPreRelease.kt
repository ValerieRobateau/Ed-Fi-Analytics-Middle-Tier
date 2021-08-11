package _self.preRelease.buildTypes

import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.dotnetPublish
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.powerShell

object BuildAndTestPreRelease : BuildType({
    templates(_self.templates.BuildAndTestNetCoreApplication)
    name = "Build and Test Pre-Release"

    triggers {
        vcs {
            id = "vcsTrigger"
            enabled = false
            branchFilter = "+:main"
        }
    }
})