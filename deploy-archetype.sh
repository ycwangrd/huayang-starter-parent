#!/bin/zsh
# mvn archetype:create-from-project –DgroupId=jdp.huayanginfo(在项目根目录执行)
# 执行完毕后，切换到target\generated-sources\archetype下，执行mvn install 或者 mvn deploy
rm -rf ./logs
rm -rf ./target
mvn deploy
cd project-starter
rm -rf ./logs
mvn archetype:create-from-project -Darchetype.encoding=utf-8
cd ./target/generated-sources/archetype
mvn deploy
cd ../../../
mvn clean -U