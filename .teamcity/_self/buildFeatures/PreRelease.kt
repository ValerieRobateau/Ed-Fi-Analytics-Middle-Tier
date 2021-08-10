package _self.Project

import jetbrains.buildServer.configs.kotlin.v2019_2.*

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