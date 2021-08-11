package _self.templates

import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.commitStatusPublisher
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.swabra
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.dotnetBuild
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.dotnetTest
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.powerShell

object BuildAndTestNetCoreApplication : Template({
    name = "Build and Test .NET Core Application"
    description = "Use for .NET Core applications with test libraries based on .NET 4.x"

    params {
        param("DotNetBuildConfiguration", "release")
    }

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        dotnetBuild {
            name = "Build"
            id = "RUNNER_176"
            projects = "%SolutionFile%"
            configuration = "%DotNetBuildConfiguration%"
            args = "/p:Version=%PreReleaseVersionNumber%"
            param("dotNetCoverage.dotCover.home.path", "%teamcity.tool.JetBrains.dotCover.CommandLineTools.DEFAULT%")
        }
        dotnetTest {
            name = "Run Integration Tests"
            id = "RUNNER_233"
            projects = "%SolutionFile%"
            configuration = "%DotNetBuildConfiguration%"
            skipBuild = true
            param("dotNetCoverage.dotCover.home.path", "%teamcity.tool.JetBrains.dotCover.CommandLineTools.DEFAULT%")
        }
    }

    triggers {
        vcs {
            id = "vcsTrigger"
            branchFilter = ""
        }
    }

    features {
        commitStatusPublisher {
            id = "BUILD_EXT_4"
            vcsRootExtId = "${DslContext.settingsRoot.id}"
            publisher = github {
                githubUrl = "https://api.github.com"
                authType = personalToken {
                    token = "zxxb4df31ef99eb9b3e58e4ff7a346e6c293a63de66adf1c09ce8448aa20aae900965a234002ad4183f775d03cbe80d301b"
                }
            }
        }
        swabra {
            id = "swabra"
            forceCleanCheckout = true
        }
    }
})