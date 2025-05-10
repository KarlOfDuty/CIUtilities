pipeline
{
  agent any
  stages
  {
    stage("Preparation")
    {
      steps
      {
        script
        {
          common = load("${env.WORKSPACE}/scripts/common.groovy")
          common.prepare_gpg_key()
        }
      }
    }
    stage('Build / Package')
    {
      parallel
      {
        stage('RHEL')
        {
          agent { dockerfile { filename 'docker/RHEL8.Dockerfile' } }
          environment
          {
            DISTRO="rhel"
            PACKAGE_NAME="karlofduty-repo"
          }
          steps
          {
            script
            {
              common.build_rpm_package(env.DISTRO, "rpm-repos/karlofduty-repo.spec", env.PACKAGE_NAME)
              env.RHEL_RPM_NAME = sh(script: "cd ${env.DISTRO} && ls ${env.PACKAGE_NAME}-*.x86_64.rpm", returnStdout: true).trim()
              env.RHEL_RPM_PATH = "${env.DISTRO}/${env.RHEL_RPM_NAME}"
              env.RHEL_SRPM_NAME = sh(script: "cd ${env.DISTRO} && ls ${env.PACKAGE_NAME}-*.src.rpm", returnStdout: true).trim()
              env.RHEL_SRPM_PATH = "${env.DISTRO}/${env.RHEL_SRPM_NAME}"
            }
            stash(includes: "${env.RHEL_RPM_PATH}, ${env.RHEL_SRPM_NAME}", name: "${env.DISTRO}-rpm")
          }
        }
        stage('Fedora')
        {
          agent { dockerfile { filename 'docker/Fedora42.Dockerfile' } }
          environment
          {
            DISTRO="fedora"
            PACKAGE_NAME="karlofduty-repo"
          }
          steps
          {
            script
            {
              common.build_rpm_package(env.DISTRO, "rpm-repos/karlofduty-repo.spec", env.PACKAGE_NAME)
              env.FEDORA_RPM_NAME = sh(script: "cd ${env.DISTRO} && ls ${env.PACKAGE_NAME}-*.x86_64.rpm", returnStdout: true).trim()
              env.FEDORA_RPM_PATH = "${env.DISTRO}/${env.FEDORA_RPM_NAME}"
              env.FEDORA_SRPM_NAME = sh(script: "cd ${env.DISTRO} && ls ${env.PACKAGE_NAME}-*.src.rpm", returnStdout: true).trim()
              env.FEDORA_SRPM_PATH = "${env.DISTRO}/${env.FEDORA_SRPM_NAME}"
            }
            stash(includes: "${env.FEDORA_RPM_PATH}, ${env.FEDORA_SRPM_PATH}", name: "${env.DISTRO}-rpm")
          }
        }
        stage('Debian')
        {
          agent
          {
            dockerfile { filename 'docker/Debian12.Dockerfile' }
          }
          environment
          {
            DEBEMAIL="xkaess22@gmail.com"
            DEBFULLNAME="Karl Essinger"
            DISTRO="debian"
            PACKAGE_ROOT="${WORKSPACE}/debian"
            PACKAGE_NAME="karlofduty-repo"
          }
          steps
          {
            sh './deb-repos/generate-deb.sh'
            script
            {
              env.DEBIAN_DEB_NAME = sh(script: "cd ${env.DISTRO} && ls ${env.PACKAGE_NAME}_*_amd64.deb", returnStdout: true).trim()
              env.DEBIAN_DEB_PATH = "${env.DISTRO}/${env.DEBIAN_DEB_NAME}"
              env.DEBIAN_DSC_NAME = sh(script: "cd ${env.DISTRO} && ls ${env.PACKAGE_NAME}_*.dsc", returnStdout: true).trim()
              env.DEBIAN_DSC_PATH = "${env.DISTRO}/${env.DEBIAN_DSC_NAME}"
              env.DEBIAN_SRC_NAME = sh(script: "cd ${env.DISTRO} && ls ${env.PACKAGE_NAME}_*.tar.xz", returnStdout: true).trim()
              env.DEBIAN_SRC_PATH = "${env.DISTRO}/${env.DEBIAN_SRC_NAME}"
            }
            stash(includes: "${env.DEBIAN_DEB_PATH}, ${env.DEBIAN_SRC_PATH}, ${env.DEBIAN_DSC_PATH}", name: "${env.DISTRO}-deb")
          }
        }
        stage('Ubuntu')
        {
          agent
          {
            dockerfile { filename 'docker/Ubuntu24.04.Dockerfile' }
          }
          environment
          {
            DEBEMAIL="xkaess22@gmail.com"
            DEBFULLNAME="Karl Essinger"
            DISTRO="ubuntu"
            PACKAGE_ROOT="${WORKSPACE}/ubuntu"
            PACKAGE_NAME="karlofduty-repo"
          }
          steps
          {
            sh './deb-repos/generate-deb.sh'
            script
            {
              env.UBUNTU_DEB_NAME = sh(script: "cd ${env.DISTRO} && ls ${env.PACKAGE_NAME}_*_amd64.deb", returnStdout: true).trim()
              env.UBUNTU_DEB_PATH = "${env.DISTRO}/${env.UBUNTU_DEB_NAME}"
              env.UBUNTU_DSC_NAME = sh(script: "cd ${env.DISTRO} && ls ${env.PACKAGE_NAME}_*.dsc", returnStdout: true).trim()
              env.UBUNTU_DSC_PATH = "${env.DISTRO}/${env.UBUNTU_DSC_NAME}"
              env.UBUNTU_SRC_NAME = sh(script: "cd ${env.DISTRO} && ls ${env.PACKAGE_NAME}_*.tar.xz", returnStdout: true).trim()
              env.UBUNTU_SRC_PATH = "${env.DISTRO}/${env.UBUNTU_SRC_NAME}"
            }
            stash(includes: "${env.UBUNTU_DEB_PATH}, ${env.UBUNTU_SRC_PATH}, ${env.UBUNTU_DSC_PATH}", name: "${env.DISTRO}-deb")
          }
        }
      }
    }
    stage('Sign')
    {
      parallel
      {
        stage('RHEL')
        {
          steps
          {
            unstash(name: 'rhel-rpm')
            script
            {
              common.sign_rpm_package(env.RHEL_RPM_PATH)
              common.sign_rpm_package(env.RHEL_SRPM_PATH)
            }
            archiveArtifacts(artifacts: "${env.RHEL_RPM_PATH}, ${env.RHEL_SRPM_PATH}", caseSensitive: true)
          }
        }
        stage('Fedora')
        {
          steps
          {
            unstash(name: 'fedora-rpm')
            script
            {
              common.sign_rpm_package(env.FEDORA_RPM_PATH)
              common.sign_rpm_package(env.FEDORA_SRPM_PATH)
            }
            archiveArtifacts(artifacts: "${env.FEDORA_RPM_PATH}, ${env.FEDORA_SRPM_PATH}", caseSensitive: true)
          }
        }
        stage('Debian')
        {
          steps
          {
            unstash(name: "debian-deb")
            script { common.sign_deb_package(env.DEBIAN_DEB_PATH, env.DEBIAN_DSC_PATH) }
            archiveArtifacts(artifacts: "${env.DEBIAN_DEB_PATH}, ${env.DEBIAN_SRC_PATH}", caseSensitive: true)
          }
        }
        stage('Ubuntu')
        {
          steps
          {
            unstash(name: "ubuntu-deb")
            script { common.sign_deb_package(env.UBUNTU_DEB_PATH, env.UBUNTU_DSC_PATH) }
            archiveArtifacts(artifacts: "${env.UBUNTU_DEB_PATH}, ${env.UBUNTU_SRC_PATH}", caseSensitive: true)
          }
        }
      }
    }
    stage('Deploy')
    {
      parallel
      {
        stage('RHEL')
        {
          when
          {
            expression { return env.BRANCH_NAME == 'main' || env.BRANCH_NAME == 'beta'; }
          }
          steps
          {
            script
            {
              common.publish_rpm_package("rhel/el8", env.RHEL_RPM_PATH, env.RHEL_SRPM_PATH, "karlofduty-repo")
              common.publish_rpm_package("rhel/el9", env.RHEL_RPM_PATH, env.RHEL_SRPM_PATH, "karlofduty-repo")
            }
            sh 'rm /usr/share/nginx/repo.karlofduty.com/rhel/karlofduty-repo-latest.x86_64.rpm || echo "Link to latest package didn\'t exist"'
            sh "ln -s /usr/share/nginx/repo.karlofduty.com/rhel/el8/x86_64/Packages/karlofduty-repo/${env.RHEL_RPM_NAME} /usr/share/nginx/repo.karlofduty.com/rhel/karlofduty-repo-latest.x86_64.rpm"
          }
        }
        stage('Fedora')
        {
          when
          {
            expression { return env.BRANCH_NAME == 'main' || env.BRANCH_NAME == 'beta'; }
          }
          steps
          {
            script
            {
              common.publish_rpm_package("fedora", env.FEDORA_RPM_PATH, env.FEDORA_SRPM_PATH, "karlofduty-repo")
            }
            sh 'rm /usr/share/nginx/repo.karlofduty.com/fedora/karlofduty-repo-latest.x86_64.rpm || echo "Link to latest package didn\'t exist"'
            sh "ln -s /usr/share/nginx/repo.karlofduty.com/fedora/x86_64/Packages/karlofduty-repo/${env.FEDORA_RPM_NAME} /usr/share/nginx/repo.karlofduty.com/fedora/karlofduty-repo-latest.x86_64.rpm"
          }
        }
        stage('Debian')
        {
          when
          {
            expression { return env.BRANCH_NAME == 'main' || env.BRANCH_NAME == 'beta'; }
          }
          environment
          {
            DISTRO="debian"
            PACKAGE_NAME="karlofduty-repo"
            COMPONENT="main"
          }
          steps
          {
            script
            {
              common.publish_deb_package(env.DISTRO, env.PACKAGE_NAME, env.PACKAGE_NAME, "${WORKSPACE}/${env.DISTRO}", env.COMPONENT)
              common.generate_debian_release_file("${WORKSPACE}", env.DISTRO)
            }
            sh "rm /usr/share/nginx/repo.karlofduty.com/${env.DISTRO}/dists/${env.DISTRO}/karlofduty-repo_latest_amd64.deb || echo \"Link to latest package didn't exist\""
            sh "ln -s /usr/share/nginx/repo.karlofduty.com/${env.DISTRO}/pool/${env.COMPONENT}/karlofduty-repo/${env.DEBIAN_DEB_NAME} /usr/share/nginx/repo.karlofduty.com/${env.DISTRO}/dists/${env.DISTRO}/karlofduty-repo_latest_amd64.deb"
          }
        }
        stage('Ubuntu')
        {
          when
          {
            expression { return env.BRANCH_NAME == 'main' || env.BRANCH_NAME == 'beta'; }
          }
          environment
          {
            DISTRO="ubuntu"
            PACKAGE_NAME="karlofduty-repo"
            COMPONENT="main"
          }
          steps
          {
            script
            {
              common.publish_deb_package(env.DISTRO, env.PACKAGE_NAME, env.PACKAGE_NAME, "${WORKSPACE}/${env.DISTRO}", env.COMPONENT)
              common.generate_debian_release_file("${WORKSPACE}", env.DISTRO)
            }
            sh "rm /usr/share/nginx/repo.karlofduty.com/${env.DISTRO}/dists/${env.DISTRO}/karlofduty-repo_latest_amd64.deb || echo \"Link to latest package didn't exist\""
            sh "ln -s /usr/share/nginx/repo.karlofduty.com/${env.DISTRO}/pool/${env.COMPONENT}/karlofduty-repo/${env.UBUNTU_DEB_NAME} /usr/share/nginx/repo.karlofduty.com/${env.DISTRO}/dists/${env.DISTRO}/karlofduty-repo_latest_amd64.deb"
          }
        }
      }
    }
  }
}
