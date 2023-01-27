import pandas as pd
import numpy as np

from sklearn.preprocessing import KBinsDiscretizer

if __name__ == '__main__':
    data = pd.read_csv('Files\MIP\converted.txt', sep=" ", header=None)
    print(data.shape)
    target = 'binarized.txt'

    fixed_data = data.iloc[:,:-1]
    print(fixed_data)
    fixed_data.to_csv(target, index=False, sep=" ", header=None) 

    est = KBinsDiscretizer(n_bins=2, encode='ordinal', strategy='kmeans')
    est.fit(fixed_data)

    xt = est.transform(fixed_data).astype(int)
    np.savetxt(target, xt, delimiter=" ", fmt='%i')