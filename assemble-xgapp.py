#!/usr/bin/env python

import argparse
import os
import urllib2
import base64
import zipfile
import sys
import json
import shutil

parser = argparse.ArgumentParser(description='Resolve release version')
parser.add_argument('-u', '--user', help='Maven user', required=True, dest="user")
parser.add_argument('-p', '--password', help='Maven password', required=True, dest="password")
parser.add_argument('-t', '--target', help='Target directory', required=True, dest="target_dir")
parser.add_argument('-l', '--pipeline', help='Pipeline zip file', required=True, dest="pipeline_file")
parser.add_argument('-c', '--configuration-file', help='Pipeline configuration file', required=True,
                    dest="pipeline_configuration_file")

args = parser.parse_args()

user = args.user
password = args.password
target_dir = args.target_dir
pipeline_file = args.pipeline_file
pipeline_configuration_file = args.pipeline_configuration_file

MAVEN_BASE = "http://maven.ontotext.com/content/repositories"


def read_configuration():
    with open(pipeline_configuration_file) as pipeline_configuration_json_file:
        configuration = json.load(pipeline_configuration_json_file)
    global repository
    repository = configuration['repository']
    global pipeline_id
    pipeline_id = configuration['pipeline_id']
    global indexes_directory_name
    indexes_directory_name = configuration['indexes_directory_name']
    global dependencies
    dependencies = configuration['dependencies']


def setup_env():
    if not os.path.exists(target_dir):
        os.makedirs(target_dir)
    if not os.path.exists(target_dir + '/' + indexes_directory_name + '/gazetteer'):
        os.makedirs(target_dir + '/' + indexes_directory_name + '/gazetteer')
    if not os.path.exists(target_dir + '/tmp'):
        os.makedirs(target_dir + '/tmp')


def process_pipeline():
    print(pipeline_id.center(80, '-'))
    if not os.path.exists(pipeline_file):
        print("Pipeline archive [%s] doesn't exist" % pipeline_file)
        sys.exit(1)
    else:
        if not os.path.exists(target_dir + '/' + pipeline_id):
            print("Unzipping pipeline [%s]..." % pipeline_file)
            unzip_file(pipeline_file, target_dir)
        else:
            print("[%s] already exists, not unzipping it again" % (target_dir + '/' + pipeline_id))


def process_pipeline_dependencies():
    for dependency in dependencies:
        group_id = dependency['groupId']
        artifact_id = dependency['artifactId']
        version = dependency['version']
        type = dependency['type']
        if 'classifier' in dependency:
            classifier = dependency['classifier']
            artifact_path = "%s/%s/%s/%s/%s/%s-%s-%s.%s" % (
                MAVEN_BASE, repository, group_id.replace('.', '/'), artifact_id, version, artifact_id, version, classifier, type)
            dependency_zip_file = target_dir + '/tmp/' + "%s-%s-%s-%s.%s" % (group_id, artifact_id, version, classifier, type)
        else:
            artifact_path = "%s/%s/%s/%s/%s/%s-%s.%s" % (
                MAVEN_BASE, repository, group_id.replace('.', '/'), artifact_id, version, artifact_id, version, type)
            dependency_zip_file = target_dir + '/tmp/' + "%s-%s-%s.%s" % (group_id, artifact_id, version, type)

        if dependency['index']:
            dependency_target_dir = target_dir + '/' + indexes_directory_name + '/' + dependency['target_dir']
        else:
            dependency_target_dir = target_dir + '/' + indexes_directory_name + '/gazetteer/' + dependency['target_dir']

        print(artifact_id.center(80, '-'))
        if not os.path.exists(dependency_zip_file):
            download_file(artifact_path, dependency_zip_file)
        else:
            print("[%s] already exists, not downloading it again" % dependency_zip_file)

        if os.path.exists(dependency_target_dir):
            print("[%s] already exists, deleting it..." % dependency_target_dir)
            shutil.rmtree(dependency_target_dir)
        print("Unzipping [%s]..." % dependency_zip_file)
        unzip_file(dependency_zip_file, dependency_target_dir)


def unzip_file(zip_file, target):
    fh = open(zip_file, 'rb')
    z = zipfile.ZipFile(fh)
    for name in z.namelist():
        z.extract(name, target)
    fh.close()


def download_file(url, target):
    request = urllib2.Request(url)
    base64string = base64.encodestring('%s:%s' % (user, password)).replace('\n', '')
    request.add_header("Authorization", "Basic %s" % base64string)

    u = urllib2.urlopen(request)
    f = open(target, 'wb')
    meta = u.info()
    file_size = int(meta.getheaders("Content-Length")[0])
    print "Downloading: [%s]" % url
    print "Target: [%s]" % target
    print "Size(in Bytes): [%s]" % file_size

    file_size_dl = 0
    block_sz = 8192
    while True:
        buffer = u.read(block_sz)
        if not buffer:
            break

        file_size_dl += len(buffer)
        f.write(buffer)
        status = r"%10d  [%3.2f%%]" % (file_size_dl, file_size_dl * 100. / file_size)
        status += chr(8) * (len(status) + 1)
        print status,
    f.close()


if __name__ == "__main__":
    print("Assemble pipeline started...")
    read_configuration()
    setup_env()
    process_pipeline()
    process_pipeline_dependencies()
    print("Done.")
