package _self.Project

import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.commitStatusPublisher
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.swabra
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.dotnetBuild
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.dotnetPublish
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.dotnetTest
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.powerShell
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2019_2.vcs.GitVcsRoot

object PreRelease : Project({
    name = "Pre-Release"
    description = "Build configurations for pre-release work"

    buildType(BuildAndTestPullRequest)
    buildType(BuildAndTestPullRequest_2)
    buildType(PublishPreReleasePackages)
})

object Release : Project({
    name = "Release"
    description = "Build configurations for release (master branch) work"

    buildType(BuildAndTestReleaseCandidate)
    buildType(PublishReleasePackages)

    params {
        param("SemanticVersionNumber", "2.5.0")
    }
})