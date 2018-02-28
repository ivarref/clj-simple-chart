#!/usr/bin/env bash

set -ex
rm -fv 0.png
rm -fv 1.png

curl https://raw.githubusercontent.com/ivarref/clj-simple-chart/075728e7122efc52ef34b468294444c207f68e44/img/ncs-png/discovery-overview.png -o 0.png
curl https://raw.githubusercontent.com/ivarref/clj-simple-chart/master/img/ncs-png/discovery-overview.png -o 1.png
montage [0-1].png -tile 2x1 -geometry +0+0 -scale 50% oil.png

curl https://raw.githubusercontent.com/ivarref/clj-simple-chart/075728e7122efc52ef34b468294444c207f68e44/img/ncs-png/discovery-overview-gas.png -o 0.png
curl https://raw.githubusercontent.com/ivarref/clj-simple-chart/master/img/ncs-png/discovery-overview-gas.png -o 1.png
montage [0-1].png -tile 2x1 -geometry +0+0 -scale 50% gas.png

curl https://raw.githubusercontent.com/ivarref/clj-simple-chart/075728e7122efc52ef34b468294444c207f68e44/img/ncs-png/discovery-overview-petroleum.png -o 0.png
curl https://raw.githubusercontent.com/ivarref/clj-simple-chart/master/img/ncs-png/discovery-overview-petroleum.png -o 1.png
montage [0-1].png -tile 2x1 -geometry +0+0 -scale 50% petroleum.png

rm -fv 0.png
rm -fv 1.png
