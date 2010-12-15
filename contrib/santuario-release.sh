#!/bin/sh
#
# Copyright (c) The Apache Software Foundation.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#
if [ -z "${SANTUARIO_SRC_ROOT}" ]
then
    echo "Assuming Santuario source tree is the CWD..."
    SANTUARIO_SRC_ROOT=`pwd`
fi
if [ -z "${SANTUARIO_VERSION}" ]
then
    SANTUARIO_VERSION=SNAPSHOT
    echo "Setting SANTUARIO_VERSION to ${SANTUARIO_VERSION}"
fi
if [ -z "${SANTUARIO_STAGE_ROOT}" ]
then
    SANTUARIO_STAGE_ROOT=/tmp/$(id -u -nr)/stage_santuario/${SANTUARIO_VERSION}
    echo "Setting SANTUARIO_STAGE_ROOT to ${SANTUARIO_STAGE_ROOT}"
fi
#
# set up the staging area
#
rm -rf ${SANTUARIO_STAGE_ROOT}
mkdir -p ${SANTUARIO_STAGE_ROOT}/dist
mkdir -p ${SANTUARIO_STAGE_ROOT}/maven/org/apache/santuario/xmlsec/${SANTUARIO_VERSION}
#
# Build and stage the distribution using ant
#
cd ${SANTUARIO_SRC_ROOT}
ant clean
ant dist || exit 1
cp -r build/*.zip ${SANTUARIO_STAGE_ROOT}/dist
cp pom.xml ${SANTUARIO_STAGE_ROOT}/maven/org/apache/santuario/xmlsec/${SANTUARIO_VERSION}/xmlsec-${SANTUARIO_VERSION}.pom
cp build/xmlsec-${SANTUARIO_VERSION}.jar ${SANTUARIO_STAGE_ROOT}/maven/org/apache/santuario/xmlsec/${SANTUARIO_VERSION}

#
# Sign and hash the release bits
#
cd ${SANTUARIO_STAGE_ROOT}/dist
for i in *
do
    gpg --detach-sign --armor $i
    gpg --verify $i.asc
done
for i in *.zip
do
    md5sum $i > $i.md5
done
cd ${SANTUARIO_STAGE_ROOT}/maven/org/apache/santuario/xmlsec/${SANTUARIO_VERSION}
for i in *
do
    gpg --detach-sign --armor $i
    gpg --verify $i.asc
done
for i in *.jar *.pom
do
    md5sum $i > $i.md5
done
#
# Build the web site
#
cd ${SANTUARIO_SRC_ROOT}
ant xdocs || exit 1
cp -r doc/site/build/site ${SANTUARIO_STAGE_ROOT}

