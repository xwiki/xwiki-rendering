/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

// It's assumed that Jenkins has been configured to implicitly load the vars/*.groovy libraries.
// Note that the version used is the one defined in Jenkins but it can be overridden as follows:
// @Library("XWiki@<branch, tag, sha1>") _
// See https://github.com/jenkinsci/workflow-cps-global-lib-plugin for details.

def globalMavenOpts = '-Xmx1024m -Xms256m'

stage ('Rendering Builds') {
  parallel(
    'main': {
      node {
        // Build, skipping quality checks so that the result of the build can be sent as fast as possible to the devs.
        // In addition, we want the generated artifacts to be deployed to our remote Maven repository so that developers
        // can benefit from them even though some quality checks have not yet passed. In // we start a build with the
        // quality profile that executes various quality checks.
        xwikiBuild('Main') {
          xvnc = false
          mavenOpts = globalMavenOpts
          profiles = '-repository-all,repository-snapshots,legacy,integration-tests,standalone'
          properties = '-Dxwiki.checkstyle.skip=true -Dxwiki.surefire.captureconsole.skip=true -Dxwiki.revapi.skip=true'
          javadoc = false
        }
      }
    },
    'testrelease': {
      node {
        // Simulate a release and verify all is fine, in preparation for the release day.
        xwikiBuild('TestRelease') {
          xvnc = false
          mavenOpts = globalMavenOpts
          goals = 'clean install'
          profiles = '-repository-all,repository-snapshots,legacy,integration-tests,standalone'
          properties = '-DskipTests -DperformRelease=true -Dgpg.skip=true -Dxwiki.checkstyle.skip=true -Ddoclint=all'
          javadoc = false
        }
      }
    },
    'quality': {
      node {
        // Run the quality checks.
        // Sonar notes:
        // - we need sonar:sonar to perform the analysis
        // - we need sonar = true to push the analysis to Sonarcloud
        // - we need jacoco:report to execute jacoco and compute test coverage
        // - we need -Pcoverage and -Dxwiki.jacoco.itDestFile to tell Jacoco to compute a single global Jacoco
        //   coverage for the full reactor (so that the coverage percentage computed takes into account module tests
        //   which cover code in other modules)
        xwikiBuild('Quality') {
          xvnc = false
          mavenOpts = globalMavenOpts
          goals = 'clean install jacoco:report sonar:sonar'
          profiles = '-repository-all,repository-snapshots,quality,legacy,coverage'
          properties = '-Dxwiki.jacoco.itDestFile=`pwd`/target/jacoco-it.exec'
          sonar = true
          javadoc = false
          // Build with Java 14 since Sonar requires Java 11+ and we want at the same time to verify that XWiki builds
          // with the latest Java version.
          javaTool = 'java14'
        }
      }
    },
    'checkstyle': {
      node {
        // Build with checkstyle. Make sure "mvn checkstyle:check" passes so that we don't cause false positive on
        // Checkstyle side. This is for the Checkstyle project itself so that they can verify that when they bring
        // changes to Checkstyle, there's no regression to the XWiki build.
        xwikiBuild('Checkstyle') {
          xvnc = false
          mavenOpts = globalMavenOpts
          goals = 'clean test-compile checkstyle:check@default'
          profiles = '-repository-all,repository-snapshots,legacy'
          javadoc = false
        }
      }
    }
  )

  // If the job is successful, trigger the platform job
  if (currentBuild.result == 'SUCCESS') {
    build job: "../xwiki-platform/${env.BRANCH_NAME}", wait: false
  }
}
