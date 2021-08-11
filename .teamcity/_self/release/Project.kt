package _self.release

import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.vcs.GitVcsRoot
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.swabra
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.dotnetBuild
import jetbrains.buildServer.configs.kotlin.v2019_2.Project


object Release : Project({
    name = "Release"
    description = "Build configurations for release (master branch) work"

    buildType(_self.release.buildTypes.BuildAndTestReleaseCandidate)
    buildType(_self.release.buildTypes.PublishReleasePackages)

    params {
        param("SemanticVersionNumber", "2.5.0")
    }
})