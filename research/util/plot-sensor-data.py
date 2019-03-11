#!/usr/bin/env python3

import csv
import matplotlib.pyplot as plt
import sys

#filename = "../research/accelerometer-data-tapping-20190115-1212.csv"
#filename = "../research/gyroscope-data-tapping-20190115-1212.csv"
filename = "../research/accelerometer-data-sweep-jbl4-20190117-1417.csv"

if len(sys.argv) > 1:
    filename = sys.argv[1]

with open(filename) as f:
    reader = csv.reader(f, delimiter=";")
    values = list(reader)

values = [ (float(x[0]), float(x[1]), float(x[2]), float(x[3])) for x in values ]

timestamps = [ x[0] for x in values ]
xs = [ x[1] for x in values ]
ys = [ x[2] for x in values ]
zs = [ x[3] for x in values ]

plt.plot(xs)
plt.plot(ys)
plt.plot(zs)

plt.show()
