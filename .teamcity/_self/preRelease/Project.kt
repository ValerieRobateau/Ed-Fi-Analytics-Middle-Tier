package _self.preRelease

import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.vcs.GitVcsRoot
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.swabra
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.dotnetBuild
import jetbrains.buildServer.configs.kotlin.v2019_2.Project


object PreRelease : Project({
    name = "Pre-Release"
    description = "Build configurations for pre-release work"

    buildType(_self.preRelease.buildTypes.BuildAndTestPreRelease)
    buildType(_self.preRelease.buildTypes.BuildAndTestPullRequest)
    buildType(_self.preRelease.buildTypes.PublishPreReleasePackages)
})

