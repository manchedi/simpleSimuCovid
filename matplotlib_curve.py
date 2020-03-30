import pandas as pd
import matplotlib.pyplot as plt

df8 = pd.read_csv("Reqsimu.csv")
plt.plot(df8['day'], df8['infected'])
plt.plot(df8['day'], df8['byFamily'])
plt.plot(df8['day'], df8['byWork'])
plt.plot(df8['day'], df8['bySchool'])
plt.plot(df8['day'], df8['immunized'])
plt.xlabel('days')
plt.legend(['infected','byFamily','byWork','bySchool','immunized'])