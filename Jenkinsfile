pipeline
{
  agent any
  stages
  {
    stage('Build / Package')
    {
      parallel
      {
        stage('RHEL')
        {
          agent
          {
            dockerfile { filename 'docker/RHEL8.Dockerfile' }
          }
          steps
          {
            sh 'rpmbuild -bb rpm-repos/karlofduty-repo.spec --define "_topdir $PWD/rhel" --define "distro rhel"'
            sh 'cp rhel/RPMS/x86_64/karlofduty-repo-*.x86_64.rpm rhel/'
            stash includes: 'rhel/karlofduty-repo-*.x86_64.rpm', name: 'rhel-rpm'
          }
        }
        stage('Fedora')
        {
          agent
          {
            dockerfile { filename 'docker/Fedora.Dockerfile' }
          }
          steps
          {
            sh 'rpmbuild -bb rpm-repos/karlofduty-repo.spec --define "_topdir $PWD/fedora" --define "distro fedora"'
            sh 'cp fedora/RPMS/x86_64/karlofduty-repo-*.x86_64.rpm fedora/'
            stash includes: 'fedora/karlofduty-repo-*.x86_64.rpm', name: 'fedora-rpm'
          }
        }
        //stage('Debian')
        //{
        //  agent
        //  {
        //    dockerfile { filename 'docker/Debian.Dockerfile' }
        //  }
        //  environment
        //  {
        //    DEBEMAIL="xkaess22@gmail.com"
        //    DEBFULLNAME="Karl Essinger"
        //    PACKAGE_ROOT="${WORKSPACE}/debian"
        //  }
        //  steps
        //  {
        //    sh './packaging/generate-deb.sh'
        //    archiveArtifacts(artifacts: 'debian/supportboi-dev_*_amd64.deb, debian/supportboi-dev_*.tar.xz', caseSensitive: true)
        //    stash includes: 'debian/supportboi-dev_*_amd64.deb, debian/supportboi-dev_*.tar.xz, debian/supportboi-dev_*.dsc', name: 'debian-deb'
        //  }
        //}
        //stage('Ubuntu')
        //{
        //  agent
        //  {
        //    dockerfile { filename 'docker/Ubuntu.Dockerfile' }
        //  }
        //  environment
        //  {
        //    DEBEMAIL="xkaess22@gmail.com"
        //    DEBFULLNAME="Karl Essinger"
        //    PACKAGE_ROOT="${WORKSPACE}/ubuntu"
        //  }
        //  steps
        //  {
        //    sh './packaging/generate-deb.sh'
        //    archiveArtifacts(artifacts: 'ubuntu/supportboi-dev_*_amd64.deb, ubuntu/supportboi-dev_*.orig.tar.gz, ubuntu/supportboi-dev_*.tar.xz', caseSensitive: true)
        //    stash includes: 'ubuntu/supportboi-dev_*_amd64.deb, ubuntu/supportboi-dev_*.tar.xz, ubuntu/supportboi-dev_*.dsc', name: 'ubuntu-deb'
        //  }
        //}
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
            withCredentials([string(credentialsId: 'JENKINS_GPG_KEY_PASSWORD', variable: 'JENKINS_GPG_KEY_PASSWORD')]) {
              sh '/usr/lib/gnupg/gpg-preset-passphrase --passphrase "$JENKINS_GPG_KEY_PASSWORD" --preset 0D27E4CD885E9DD79C252E825F70A1590922C51E'
              sh 'rpmsign --define "_gpg_name Karl Essinger (Jenkins Signing) <xkaess22@gmail.com>" --addsign rhel/karlofduty-repo-*.x86_64.rpm'
              sh 'rpm -vv --checksig rhel/karlofduty-repo-*.x86_64.rpm'
            }
            archiveArtifacts(artifacts: 'rhel/karlofduty-repo-*.x86_64.rpm', caseSensitive: true)
          }
        }
        stage('Fedora')
        {
          steps
          {
            unstash name: 'fedora-rpm'
            withCredentials([string(credentialsId: 'JENKINS_GPG_KEY_PASSWORD', variable: 'JENKINS_GPG_KEY_PASSWORD')]) {
              sh '/usr/lib/gnupg/gpg-preset-passphrase --passphrase "$JENKINS_GPG_KEY_PASSWORD" --preset 0D27E4CD885E9DD79C252E825F70A1590922C51E'
              sh 'rpmsign --define "_gpg_name Karl Essinger (Jenkins Signing) <xkaess22@gmail.com>" --addsign fedora/karlofduty-repo-*.x86_64.rpm'
              sh 'rpm -vv --checksig fedora/karlofduty-repo-*.x86_64.rpm'
            }
            archiveArtifacts(artifacts: 'fedora/karlofduty-repo-*.x86_64.rpm', caseSensitive: true)
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
        //stage('Debian')
        //{
        //  when
        //  {
        //    expression { return env.BRANCH_NAME == 'main' || env.BRANCH_NAME == 'beta'; }
        //  }
        //  environment
        //  {
        //    DISTRO="debian"
        //    REPO_DIR="/usr/share/nginx/repo.karlofduty.com/${env.DISTRO}"
        //    POOL_DIR="${env.REPO_DIR}/pool/dev/supportboi"
        //    DISTS_BIN_DIR="${env.REPO_DIR}/dists/${env.DISTRO}/dev/binary-amd64"
        //    DISTS_SRC_DIR="${env.REPO_DIR}/dists/${env.DISTRO}/dev/source"
        //  }
        //  steps
        //  {
        //    unstash name: "${env.DISTRO}-deb"
//
        //    // Copy package and sources to pool directory
        //    sh "mkdir -p ${env.POOL_DIR}"
        //    sh "cp ${env.DISTRO}/supportboi-dev_*_amd64.deb ${env.POOL_DIR}"
        //    sh "cp ${env.DISTRO}/supportboi-dev_*.tar.xz ${env.POOL_DIR}"
        //    sh "cp ${env.DISTRO}/supportboi-dev_*.dsc ${env.POOL_DIR}"
        //    dir("${env.REPO_DIR}")
        //    {
        //      // Generate package lists
        //      sh "mkdir -p ${env.DISTS_BIN_DIR}"
        //      sh "dpkg-scanpackages --arch amd64 -m pool/ > ${env.DISTS_BIN_DIR}/Packages"
        //      sh "cat ${env.DISTS_BIN_DIR}/Packages | gzip -9c > ${env.DISTS_BIN_DIR}/Packages.gz"
//
        //      // Generate source lists
        //      sh "mkdir -p ${env.DISTS_SRC_DIR}"
        //      sh "dpkg-scansources pool/ > ${env.DISTS_SRC_DIR}/Sources"
        //      sh "cat ${env.DISTS_SRC_DIR}/Sources | gzip -9c > ${env.DISTS_SRC_DIR}/Sources.gz"
        //    }
//
        //    dir("${env.REPO_DIR}/dists/${env.DISTRO}")
        //    {
        //      // Generate release file
        //      sh "${WORKSPACE}/CIUtilities/scripts/generate-deb-release-file.sh > Release"
        //    }
//
        //    sh "rmdir ${env.REPO_DIR}@tmp"
        //  }
        //}
        //stage('Ubuntu')
        //{
        //  when
        //  {
        //    expression { return env.BRANCH_NAME == 'main' || env.BRANCH_NAME == 'beta'; }
        //  }
        //  environment
        //  {
        //    DISTRO="ubuntu"
        //    REPO_DIR="/usr/share/nginx/repo.karlofduty.com/${env.DISTRO}"
        //    POOL_DIR="${env.REPO_DIR}/pool/dev/supportboi"
        //    DISTS_BIN_DIR="${env.REPO_DIR}/dists/${env.DISTRO}/dev/binary-amd64"
        //    DISTS_SRC_DIR="${env.REPO_DIR}/dists/${env.DISTRO}/dev/source"
        //  }
        //  steps
        //  {
        //    unstash name: "${env.DISTRO}-deb"
//
        //    // Copy package and sources to pool directory
        //    sh "mkdir -p ${env.POOL_DIR}"
        //    sh "cp ${env.DISTRO}/supportboi-dev_*_amd64.deb ${env.POOL_DIR}"
        //    sh "cp ${env.DISTRO}/supportboi-dev_*.tar.xz ${env.POOL_DIR}"
        //    sh "cp ${env.DISTRO}/supportboi-dev_*.dsc ${env.POOL_DIR}"
        //    dir("${env.REPO_DIR}")
        //    {
        //      // Generate package lists
        //      sh "mkdir -p ${env.DISTS_BIN_DIR}"
        //      sh "dpkg-scanpackages --arch amd64 -m pool/ > ${env.DISTS_BIN_DIR}/Packages"
        //      sh "cat ${env.DISTS_BIN_DIR}/Packages | gzip -9c > ${env.DISTS_BIN_DIR}/Packages.gz"
//
        //      // Generate source lists
        //      sh "mkdir -p ${env.DISTS_SRC_DIR}"
        //      sh "dpkg-scansources pool/ > ${env.DISTS_SRC_DIR}/Sources"
        //      sh "cat ${env.DISTS_SRC_DIR}/Sources | gzip -9c > ${env.DISTS_SRC_DIR}/Sources.gz"
        //    }
//
        //    dir("${env.REPO_DIR}/dists/${env.DISTRO}")
        //    {
        //      // Generate release file
        //      sh "${WORKSPACE}/CIUtilities/scripts/generate-deb-release-file.sh > Release"
        //    }
//
        //    sh "rmdir ${env.REPO_DIR}@tmp"
        //  }
        //}
      }
    }
  }
}
