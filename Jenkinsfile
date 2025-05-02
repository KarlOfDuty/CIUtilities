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
            script
            {
              env.RHEL_RPM_NAME = sh(script: "cd ${env.DISTRO} && ls karlofduty-repo-*.x86_64.rpm", returnStdout: true).trim()
              env.RHEL_RPM_PATH = "${env.DISTRO}/${env.RHEL_RPM_NAME}"
            }
            stash includes: "${env.DISTRO}/karlofduty-repo-*.x86_64.rpm", name: "${env.DISTRO}-rpm"
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
            sh "tree ${env.DISTRO} -L 3"
            sh "cp ${env.DISTRO}/RPMS/x86_64/karlofduty-repo-*.x86_64.rpm ${env.DISTRO}/"
            script
            {
              env.FEDORA_RPM_NAME = sh(script: "cd ${env.DISTRO} && ls karlofduty-repo-*.x86_64.rpm", returnStdout: true).trim()
              env.FEDORA_RPM_PATH = "${env.DISTRO}/${env.FEDORA_RPM_NAME}"
            }
            stash includes: "${env.DISTRO}/karlofduty-repo-*.x86_64.rpm", name: "${env.DISTRO}-rpm"
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
            sh "rpm -vv --checksig ${env.RHEL_RPM_PATH}"
            archiveArtifacts(artifacts: "${env.RHEL_RPM_PATH}", caseSensitive: true)
          }
        }
        stage('Fedora')
        {
          steps
          {
            unstash name: 'fedora-rpm'
            sh "rpmsign --define '_gpg_name Karl Essinger (Jenkins Signing) <xkaess22@gmail.com>' --addsign ${env.FEDORA_RPM_PATH}"
            sh "rpm -vv --checksig ${env.FEDORA_RPM_PATH}"
            archiveArtifacts(artifacts: "${env.FEDORA_RPM_PATH}", caseSensitive: true)
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
            sh 'mkdir -p /usr/share/nginx/repo.karlofduty.com/rhel/el8/packages/karlofduty-repo/'
            sh 'mkdir -p /usr/share/nginx/repo.karlofduty.com/rhel/el9/packages/karlofduty-repo/'
            sh 'cp rhel/karlofduty-repo-*.x86_64.rpm /usr/share/nginx/repo.karlofduty.com/rhel/el8/packages/karlofduty-repo/'
            sh 'cp rhel/karlofduty-repo-*.x86_64.rpm /usr/share/nginx/repo.karlofduty.com/rhel/el9/packages/karlofduty-repo/'
            sh 'rm /usr/share/nginx/repo.karlofduty.com/rhel/karlofduty-repo-latest.x86_64.rpm || echo "Link to latest package didn\'t exist"'
            sh 'ln -s /usr/share/nginx/repo.karlofduty.com/rhel/el8/packages/karlofduty-repo/$(cd rhel && ls karlofduty-repo-*.x86_64.rpm) /usr/share/nginx/repo.karlofduty.com/rhel/karlofduty-repo-latest.x86_64.rpm'
            sh 'createrepo_c --update /usr/share/nginx/repo.karlofduty.com/rhel/el8'
            sh 'createrepo_c --update /usr/share/nginx/repo.karlofduty.com/rhel/el9'
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
            sh 'mkdir -p /usr/share/nginx/repo.karlofduty.com/fedora/packages/karlofduty-repo/'
            sh 'cp fedora/karlofduty-repo-*.x86_64.rpm /usr/share/nginx/repo.karlofduty.com/fedora/packages/karlofduty-repo/'
            sh 'rm /usr/share/nginx/repo.karlofduty.com/fedora/karlofduty-repo-latest.x86_64.rpm || echo "Link to latest package didn\'t exist"'
            sh 'ln -s /usr/share/nginx/repo.karlofduty.com/fedora/packages/karlofduty-repo/$(cd fedora && ls karlofduty-repo-*.x86_64.rpm) /usr/share/nginx/repo.karlofduty.com/fedora/karlofduty-repo-latest.x86_64.rpm'
            sh 'createrepo_c --update /usr/share/nginx/repo.karlofduty.com/fedora'
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
