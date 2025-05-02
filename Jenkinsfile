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
          environment { DISTRO="rhel" }
          steps
          {
            sh "mkdir -p ${env.DISTRO}/SOURCES"
            sh "git archive --format=tar.gz HEAD > '${env.DISTRO}/SOURCES/rpm-source.tar.gz'"
            sh "rpmbuild -ba rpm-repos/karlofduty-repo.spec --define \"_topdir ${WORKSPACE}/${env.DISTRO}\" --define 'distro ${env.DISTRO}'"
            sh "cp ${env.DISTRO}/RPMS/x86_64/karlofduty-repo-*.x86_64.rpm ${env.DISTRO}/"
            sh "cp ${env.DISTRO}/SRPMS/karlofduty-repo-*.src.rpm ${env.DISTRO}/"
            script
            {
              env.RHEL_RPM_NAME = sh(script: "cd ${env.DISTRO} && ls karlofduty-repo-*.x86_64.rpm", returnStdout: true).trim()
              env.RHEL_RPM_PATH = "${env.DISTRO}/${env.RHEL_RPM_NAME}"
              env.RHEL_SRPM_NAME = sh(script: "cd ${env.DISTRO} && ls karlofduty-repo-*.src.rpm", returnStdout: true).trim()
              env.RHEL_SRPM_NAME = "${env.DISTRO}/${env.RHEL_RPM_NAME}"
            }
            stash includes: "${env.RHEL_RPM_PATH}, ${env.RHEL_SRPM_NAME}", name: "${env.DISTRO}-rpm"
          }
        }
        stage('Fedora')
        {
          agent { dockerfile { filename 'docker/Fedora.Dockerfile' } }
          environment { DISTRO="fedora" }
          steps
          {
            sh "mkdir -p ${env.DISTRO}/SOURCES"
            sh "git archive --format=tar.gz HEAD > '${env.DISTRO}/SOURCES/rpm-source.tar.gz'"
            sh "rpmbuild -ba rpm-repos/karlofduty-repo.spec --define \"_topdir ${WORKSPACE}/${env.DISTRO}\" --define 'distro ${env.DISTRO}'"
            sh "cp ${env.DISTRO}/RPMS/x86_64/karlofduty-repo-*.x86_64.rpm ${env.DISTRO}/"
            sh "cp ${env.DISTRO}/SRPMS/karlofduty-repo-*.src.rpm ${env.DISTRO}/"
            script
            {
              env.FEDORA_RPM_NAME = sh(script: "cd ${env.DISTRO} && ls karlofduty-repo-*.x86_64.rpm", returnStdout: true).trim()
              env.FEDORA_RPM_PATH = "${env.DISTRO}/${env.FEDORA_RPM_NAME}"
              env.FEDORA_SRPM_NAME = sh(script: "cd ${env.DISTRO} && ls karlofduty-repo-*.src.rpm", returnStdout: true).trim()
              env.FEDORA_SRPM_PATH = "${env.DISTRO}/${env.FEDORA_SRPM_NAME}"
            }
            stash includes: "${env.FEDORA_RPM_PATH}, ${env.FEDORA_SRPM_PATH}", name: "${env.DISTRO}-rpm"
          }
        }
        stage('Debian')
        {
          agent
          {
            dockerfile { filename 'docker/Debian.Dockerfile' }
          }
          environment
          {
            DEBEMAIL="xkaess22@gmail.com"
            DEBFULLNAME="Karl Essinger"
            DISTRO="debian"
            PACKAGE_ROOT="${WORKSPACE}/debian"
          }
          steps
          {
            sh './deb-repos/generate-deb.sh'
            script
            {
              env.DEBIAN_DEB_NAME = sh(script: "cd ${env.DISTRO} && ls karlofduty-repo_*_amd64.deb", returnStdout: true).trim()
              env.DEBIAN_DEB_PATH = "${env.DISTRO}/${env.DEBIAN_DEB_NAME}"
              env.DEBIAN_DSC_NAME = sh(script: "cd ${env.DISTRO} && ls karlofduty-repo_*.dsc", returnStdout: true).trim()
              env.DEBIAN_DSC_PATH = "${env.DISTRO}/${env.DEBIAN_DSC_NAME}"
            }
            stash includes: "${env.DEBIAN_DEB_PATH}, ${env.DISTRO}/karlofduty-repo_*.tar.xz, ${env.DEBIAN_DSC_PATH}", name: "${env.DISTRO}-deb"
          }
        }
        stage('Ubuntu')
        {
          agent
          {
            dockerfile { filename 'docker/Ubuntu.Dockerfile' }
          }
          environment
          {
            DEBEMAIL="xkaess22@gmail.com"
            DEBFULLNAME="Karl Essinger"
            DISTRO="ubuntu"
            PACKAGE_ROOT="${WORKSPACE}/ubuntu"
          }
          steps
          {
            sh './deb-repos/generate-deb.sh'
            script
            {
              env.UBUNTU_DEB_NAME = sh(script: "cd ${env.DISTRO} && ls karlofduty-repo_*_amd64.deb", returnStdout: true).trim()
              env.UBUNTU_DEB_PATH = "${env.DISTRO}/${env.UBUNTU_DEB_NAME}"
              env.UBUNTU_DSC_NAME = sh(script: "cd ${env.DISTRO} && ls karlofduty-repo_*.dsc", returnStdout: true).trim()
              env.UBUNTU_DSC_PATH = "${env.DISTRO}/${env.UBUNTU_DSC_NAME}"
            }
            stash includes: "${env.UBUNTU_DEB_PATH}, ${env.DISTRO}/karlofduty-repo_*.tar.xz, ${env.UBUNTU_DSC_PATH}", name: "${env.DISTRO}-deb"
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
            unstash name: 'rhel-rpm'
            sh "rpmsign --define '_gpg_name Karl Essinger (Jenkins Signing) <xkaess22@gmail.com>' --addsign ${env.RHEL_RPM_PATH}"
            sh "rpmsign --define '_gpg_name Karl Essinger (Jenkins Signing) <xkaess22@gmail.com>' --addsign ${env.RHEL_SRPM_PATH}"
            sh "rpm -vv --checksig ${env.RHEL_RPM_PATH}"
            sh "rpm -vv --checksig ${env.RHEL_SRPM_PATH}"
            archiveArtifacts(artifacts: "${env.RHEL_RPM_PATH}, ${env.RHEL_SRPM_PATH}", caseSensitive: true)
          }
        }
        stage('Fedora')
        {
          steps
          {
            unstash name: 'fedora-rpm'
            sh "rpmsign --define '_gpg_name Karl Essinger (Jenkins Signing) <xkaess22@gmail.com>' --addsign ${env.FEDORA_RPM_PATH}"
            sh "rpmsign --define '_gpg_name Karl Essinger (Jenkins Signing) <xkaess22@gmail.com>' --addsign ${env.FEDORA_SRPM_PATH}"
            sh "rpm -vv --checksig ${env.FEDORA_RPM_PATH}"
            sh "rpm -vv --checksig ${env.FEDORA_SRPM_PATH}"
            archiveArtifacts(artifacts: "${env.FEDORA_RPM_PATH}, ${env.FEDORA_SRPM_PATH}", caseSensitive: true)
          }
        }
        stage('Debian')
        {
          steps
          {
            unstash name: "debian-deb"
            script { common.sign_deb_package(env.DEBIAN_DEB_PATH, env.DEBIAN_DSC_PATH) }
            archiveArtifacts(artifacts: "${env.DEBIAN_DEB_PATH}, debian/karlofduty-repo_*.tar.xz", caseSensitive: true)
          }
        }
        stage('Ubuntu')
        {
          steps
          {
            unstash name: "ubuntu-deb"
            script { common.sign_deb_package(env.UBUNTU_DEB_PATH, env.UBUNTU_DSC_PATH) }
            archiveArtifacts(artifacts: "${env.UBUNTU_DEB_PATH}, ubuntu/karlofduty-repo_*.tar.xz", caseSensitive: true)
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
            sh 'mkdir -p /usr/share/nginx/repo.karlofduty.com/rhel/el8/x86_64/packages/karlofduty-repo/'
            sh 'mkdir -p /usr/share/nginx/repo.karlofduty.com/rhel/el9/x86_64/packages/karlofduty-repo/'
            sh 'mkdir -p /usr/share/nginx/repo.karlofduty.com/rhel/el8/source/packages/karlofduty-repo/'
            sh 'mkdir -p /usr/share/nginx/repo.karlofduty.com/rhel/el9/source/packages/karlofduty-repo/'
            sh "cp ${env.RHEL_RPM_PATH} /usr/share/nginx/repo.karlofduty.com/rhel/el8/x86_64/Packages/karlofduty-repo/"
            sh "cp ${env.RHEL_RPM_PATH} /usr/share/nginx/repo.karlofduty.com/rhel/el9/x86_64/Packages/karlofduty-repo/"
            sh "cp ${env.RHEL_SRPM_PATH} /usr/share/nginx/repo.karlofduty.com/rhel/el8/source/Packages/karlofduty-repo/"
            sh "cp ${env.RHEL_SRPM_PATH} /usr/share/nginx/repo.karlofduty.com/rhel/el9/source/Packages/karlofduty-repo/"

            sh 'rm /usr/share/nginx/repo.karlofduty.com/rhel/karlofduty-repo-latest.x86_64.rpm || echo "Link to latest package didn\'t exist"'
            sh "ln -s /usr/share/nginx/repo.karlofduty.com/rhel/el8/x86_64/Packages/karlofduty-repo/${env.RHEL_RPM_NAME} /usr/share/nginx/repo.karlofduty.com/rhel/karlofduty-repo-latest.x86_64.rpm"

            sh 'createrepo_c --update /usr/share/nginx/repo.karlofduty.com/rhel/el8/x86_64'
            sh 'createrepo_c --update /usr/share/nginx/repo.karlofduty.com/rhel/el9/x86_64'
            sh 'createrepo_c --update /usr/share/nginx/repo.karlofduty.com/rhel/el8/source'
            sh 'createrepo_c --update /usr/share/nginx/repo.karlofduty.com/rhel/el9/source'
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
            sh 'mkdir -p /usr/share/nginx/repo.karlofduty.com/fedora/x86_64/Packages/karlofduty-repo/'
            sh 'mkdir -p /usr/share/nginx/repo.karlofduty.com/fedora/source/Packages/karlofduty-repo/'
            sh "cp ${env.FEDORA_RPM_PATH} /usr/share/nginx/repo.karlofduty.com/fedora/x86_64/Packages/karlofduty-repo/"
            sh "cp ${env.FEDORA_SRPM_PATH} /usr/share/nginx/repo.karlofduty.com/fedora/source/Packages/karlofduty-repo/"

            sh 'rm /usr/share/nginx/repo.karlofduty.com/fedora/karlofduty-repo-latest.x86_64.rpm || echo "Link to latest package didn\'t exist"'
            sh "ln -s /usr/share/nginx/repo.karlofduty.com/fedora/x86_64/Packages/karlofduty-repo/${env.FEDORA_RPM_NAME} /usr/share/nginx/repo.karlofduty.com/fedora/karlofduty-repo-latest.x86_64.rpm"

            sh 'createrepo_c --update /usr/share/nginx/repo.karlofduty.com/fedora/x86_64'
            sh 'createrepo_c --update /usr/share/nginx/repo.karlofduty.com/fedora/source'
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
