package _self.BuildType

import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.dotnetPublish
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.powerShell

object BuildAndTestPullRequest : BuildType({
    templates(BuildAndTestNetCoreApplication)
    name = "Build and Test Pre-Release"

    triggers {
        vcs {
            id = "vcsTrigger"
            enabled = false
            branchFilter = "+:main"
        }
    }
})

object BuildAndTestPullRequest_2 : BuildType({
    templates(BuildAndTestNetCoreApplication)
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

object PublishPreReleasePackages : BuildType({
    name = "Publish Pre-Release Packages"

    enablePersonalBuilds = false
    type = BuildTypeSettings.Type.DEPLOYMENT
    buildNumberPattern = "%PreReleaseVersionNumber%"
    maxRunningBuilds = 1

    params {
        param("PublishScdZipFile", "%PublishScdDirectoryName%.zip")
        param("GitRepository", "Ed-Fi-Alliance/Ed-Fi-X-Analytics-Middle-Tier")
        param("PublishDirectory", """%teamcity.build.checkoutDir%\src\publish""")
        param("PublishFddZipFile", "%PublishFddDirectoryName%.zip")
        param("PublishFddOutputDirectory", """%PublishDirectory%\%PublishFddDirectoryName%""")
        param("GitBranchToTag", "%BranchToBuild%")
        param("GitTagName", "%PreReleaseVersionNumber%")
        param("PublishScdOutputDirectory", """%PublishDirectory%\%PublishScdDirectoryName%""")
        param("ConsoleAppDirectory", """src\%SolutionName%.Console""")
        param("GitUserName", "EdFiBuildAgent")
        param("PublishFddDirectoryName", "EdFi.AnalyticsMiddleTier")
        param("PublishScdDirectoryName", "EdFi.AnalyticsMiddleTier-win10.x64")
    }

    vcs {
        root(AnalyticsMiddleTier)
    }

    steps {
        dotnetPublish {
            name = "Publish Self-Contained Deployment"
            workingDir = "%ConsoleAppDirectory%"
            configuration = "Release"
            runtime = "win10-x64"
            outputDir = "%PublishScdOutputDirectory%"
            args = "/p:Version=%PreReleaseVersionNumber%"
            param("dotNetCoverage.dotCover.home.path", "%teamcity.tool.JetBrains.dotCover.CommandLineTools.DEFAULT%")
        }
        dotnetPublish {
            name = "Publish Framework-dependent Deployment"
            workingDir = "%ConsoleAppDirectory%"
            framework = "netcoreapp3.1"
            configuration = "Release"
            outputDir = "%PublishFddOutputDirectory%"
            args = "/p:Version=%PreReleaseVersionNumber%"
            param("dotNetCoverage.dotCover.home.path", "%teamcity.tool.JetBrains.dotCover.CommandLineTools.DEFAULT%")
        }
        powerShell {
            name = "Create Zip Files"
            formatStderrAsError = true
            scriptMode = script {
                content = """
                    if (Test-Path %PublishDirectory%\%PublishFddZipFile%) {
                        Remove-Item %PublishDirectory%\%PublishFddZipFile%
                    }
                    Compress-Archive -Path %PublishFddOutputDirectory%\* -DestinationPath %PublishDirectory%\%PublishFddZipFile%
                    
                    if (Test-Path %PublishDirectory%\%PublishScdZipFile%) {
                        Remove-Item %PublishDirectory%\%PublishScdZipFile%
                    }
                    Compress-Archive -Path %PublishScdOutputDirectory%\* -DestinationPath %PublishDirectory%\%PublishScdZipFile%
                """.trimIndent()
            }
        }
        powerShell {
            name = "Create Git Release"
            formatStderrAsError = true
            scriptMode = file {
                path = "TagAndRelease.ps1"
            }
            param("jetbrains_powershell_script_code", """
                ${'$'}accessToken = "%EdFiBuildAgentAccessToken%";
                ${'$'}scdZipFile = "%PublishScdZipFile%";
                ${'$'}fddZipFile = "%PublishFddZipFile%";
                ${'$'}publishDirectory = "%PublishDirectory%";
                ${'$'}tagName = "%GitTagName%";
                ${'$'}branch = "%GitBranchToTag%";
                ${'$'}repository = "api-testing";
                
                
                ${'$'}baseUrl = "https://api.github.com";
                ${'$'}contentType = "application/vnd.github+json";
                ${'$'}gitHubUserName = "EdFiBuildAgent";
                
                [Net.ServicePointManager]::SecurityProtocol = "tls12, tls11, tls";
                
                ${'$'}headers = @{
                    "Authorization" = "token ${'$'}accessToken";
                    "Content-Type" = ${'$'}contentType;
                    "User-Agent" = ${'$'}gitHubUserName;
                };
                
                ${'$'}body = @{
                    "tag_name" = "${'$'}tagName";
                    "target_commitish" = "${'$'}branch";
                    "name" = "${'$'}tagName";
                    "prerelease" = ${'$'}True;
                };
                
                ${'$'}path = "${'$'}baseUrl/repos/Ed-Fi-Alliance/${'$'}repository/releases";
                
                Try {
                    ${'$'}result = Invoke-RestMethod -Method POST -Uri "${'$'}path" -Headers ${'$'}headers -Body (${'$'}body|ConvertTo-Json);
                    Write-Host ${'$'}result;
                    
                    ${'$'}uploadUrl = ${'$'}result.upload_url;
                    ${'$'}headers.'Content-Type' = "application/zip";
                
                    ${'$'}uploadUrlFdd = ${'$'}uploadUrl.replace("{?name,label}", "?name=${'$'}fddZipFile");
                    ${'$'}result = Invoke-RestMethod -Method POST -Uri "${'$'}uploadUrlFdd" -Headers ${'$'}headers -Infile "${'$'}publishDirectory\${'$'}fddZipFile" -Verbose;
                    Write-Host ${'$'}result;
                
                    ${'$'}uploadUrlScd = ${'$'}uploadUrl.replace("{?name,label}", "?name=${'$'}scdZipFile");
                    ${'$'}result = Invoke-RestMethod -Method POST -Uri "${'$'}uploadUrlScd" -Headers ${'$'}headers -Infile "${'$'}publishDirectory\${'$'}scdZipFile" -Verbose;
                    Write-Host ${'$'}result;
                }
                Catch {
                    Write-Host "Web request failed for ${'$'}path with message ${'$'}_.Message";
                    Write-Error ${'$'}_;
                }
            """.trimIndent())
            param("jetbrains_powershell_scriptArguments", """
                -accessToken "%EdFiBuildAgentAccessToken%" `
                -scdZipFile "%PublishScdZipFile%" `
                -fddZipFile "%PublishFddZipFile%" `
                -publishDirectory "%PublishDirectory%" `
                -tagName "%GitTagName%" `
                -branch "%GitBranchToTag%" `
                -repository "%GitRepository%" `
                -gitHubUserName "%GitUserName%"
            """.trimIndent())
        }
    }

    dependencies {
        snapshot(BuildAndTestPullRequest) {
            runOnSameAgent = true
        }
    }
})

object BuildAndTestReleaseCandidate : BuildType({
    templates(BuildAndTestNetCoreApplication)
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

object PublishReleasePackages : BuildType({
    name = "Publish Release Packages"

    enablePersonalBuilds = false
    type = BuildTypeSettings.Type.DEPLOYMENT
    buildNumberPattern = "%SemanticVersionNumber%-%build.counter%"
    maxRunningBuilds = 1

    params {
        param("PublishScdZipFile", "%PublishScdDirectoryName%-%SemanticVersionNumber%.zip")
        param("GitRepository", "Ed-Fi-Alliance-OSS/Ed-Fi-Analytics-Middle-Tier")
        param("PublishDirectory", """%teamcity.build.checkoutDir%\src\publish""")
        param("PublishFddZipFile", "%PublishFddDirectoryName%-%SemanticVersionNumber%.zip")
        param("PublishFddOutputDirectory", """%PublishDirectory%\%PublishFddDirectoryName%""")
        param("GitBranchToTag", "main")
        param("GitTagName", "%SemanticVersionNumber%")
        param("PublishScdOutputDirectory", """%PublishDirectory%\%PublishScdDirectoryName%""")
        param("ConsoleAppDirectory", """src\%SolutionName%.Console""")
        param("GitUserName", "EdFiBuildAgent")
        param("PublishFddDirectoryName", "EdFi.AnalyticsMiddleTier")
        param("PublishScdDirectoryName", "EdFi.AnalyticsMiddleTier-win10.x64")
    }

    vcs {
        root(AnalyticsMiddleTier)
    }

    steps {
        dotnetPublish {
            name = "Publish Self-Contained Deployment"
            workingDir = "%ConsoleAppDirectory%"
            configuration = "Release"
            runtime = "win10-x64"
            outputDir = "%PublishScdOutputDirectory%"
            args = "/p:Version=%SemanticVersionNumber%"
            param("dotNetCoverage.dotCover.home.path", "%teamcity.tool.JetBrains.dotCover.CommandLineTools.DEFAULT%")
        }
        dotnetPublish {
            name = "Publish Framework-dependent Deployment"
            workingDir = "%ConsoleAppDirectory%"
            framework = "netcoreapp3.1"
            configuration = "Release"
            outputDir = "%PublishFddOutputDirectory%"
            args = "/p:Version=%SemanticVersionNumber%"
            param("dotNetCoverage.dotCover.home.path", "%teamcity.tool.JetBrains.dotCover.CommandLineTools.DEFAULT%")
        }
        powerShell {
            name = "Create Zip Files"
            formatStderrAsError = true
            scriptMode = script {
                content = """
                    if (Test-Path %PublishDirectory%\%PublishFddZipFile%) {
                        Remove-Item %PublishDirectory%\%PublishFddZipFile%
                    }
                    Compress-Archive -Path %PublishFddOutputDirectory%\* -DestinationPath %PublishDirectory%\%PublishFddZipFile%
                    
                    if (Test-Path %PublishDirectory%\%PublishScdZipFile%) {
                        Remove-Item %PublishDirectory%\%PublishScdZipFile%
                    }
                    Compress-Archive -Path %PublishScdOutputDirectory%\* -DestinationPath %PublishDirectory%\%PublishScdZipFile%
                """.trimIndent()
            }
        }
        powerShell {
            name = "Create Git Release"
            formatStderrAsError = true
            scriptMode = file {
                path = "TagAndRelease.ps1"
            }
            param("jetbrains_powershell_script_code", """
                ${'$'}accessToken = "%EdFiBuildAgentAccessToken%";
                ${'$'}scdZipFile = "%PublishScdZipFile%";
                ${'$'}fddZipFile = "%PublishFddZipFile%";
                ${'$'}publishDirectory = "%PublishDirectory%";
                ${'$'}tagName = "%GitTagName%";
                ${'$'}branch = "%GitBranchToTag%";
                ${'$'}repository = "api-testing";
                
                
                ${'$'}baseUrl = "https://api.github.com";
                ${'$'}contentType = "application/vnd.github+json";
                ${'$'}gitHubUserName = "EdFiBuildAgent";
                
                [Net.ServicePointManager]::SecurityProtocol = "tls12, tls11, tls";
                
                ${'$'}headers = @{
                    "Authorization" = "token ${'$'}accessToken";
                    "Content-Type" = ${'$'}contentType;
                    "User-Agent" = ${'$'}gitHubUserName;
                };
                
                ${'$'}body = @{
                    "tag_name" = "${'$'}tagName";
                    "target_commitish" = "${'$'}branch";
                    "name" = "${'$'}tagName";
                    "prerelease" = ${'$'}True;
                };
                
                ${'$'}path = "${'$'}baseUrl/repos/Ed-Fi-Alliance/${'$'}repository/releases";
                
                Try {
                    ${'$'}result = Invoke-RestMethod -Method POST -Uri "${'$'}path" -Headers ${'$'}headers -Body (${'$'}body|ConvertTo-Json);
                    Write-Host ${'$'}result;
                    
                    ${'$'}uploadUrl = ${'$'}result.upload_url;
                    ${'$'}headers.'Content-Type' = "application/zip";
                
                    ${'$'}uploadUrlFdd = ${'$'}uploadUrl.replace("{?name,label}", "?name=${'$'}fddZipFile");
                    ${'$'}result = Invoke-RestMethod -Method POST -Uri "${'$'}uploadUrlFdd" -Headers ${'$'}headers -Infile "${'$'}publishDirectory\${'$'}fddZipFile" -Verbose;
                    Write-Host ${'$'}result;
                
                    ${'$'}uploadUrlScd = ${'$'}uploadUrl.replace("{?name,label}", "?name=${'$'}scdZipFile");
                    ${'$'}result = Invoke-RestMethod -Method POST -Uri "${'$'}uploadUrlScd" -Headers ${'$'}headers -Infile "${'$'}publishDirectory\${'$'}scdZipFile" -Verbose;
                    Write-Host ${'$'}result;
                }
                Catch {
                    Write-Host "Web request failed for ${'$'}path with message ${'$'}_.Message";
                    Write-Error ${'$'}_;
                }
            """.trimIndent())
            param("jetbrains_powershell_scriptArguments", """
                -accessToken "%EdFiBuildAgentAccessToken%" `
                -scdZipFile "%PublishScdZipFile%" `
                -fddZipFile "%PublishFddZipFile%" `
                -publishDirectory "%PublishDirectory%" `
                -tagName "%GitTagName%" `
                -branch "%GitBranchToTag%" `
                -repository "%GitRepository%" `
                -gitHubUserName "%GitUserName%"
            """.trimIndent())
        }
    }

    failureConditions {
        errorMessage = true
    }

    dependencies {
        snapshot(BuildAndTestReleaseCandidate) {
        }
    }
})
