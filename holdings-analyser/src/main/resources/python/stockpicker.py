import pandas as pd
from sqlalchemy import create_engine
from lightgbm import LGBMClassifier
from sklearn.model_selection import TimeSeriesSplit
from sklearn.metrics import roc_auc_score

engine = create_engine("postgresql://postgres:root@localhost:5432/funds-holding-analyser")
df = pd.read_sql("SELECT * FROM training_data WHERE fwd_ret_3m IS NOT NULL", engine)

# label = stock is in top 20% of returns *that month* (cross-sectional, not absolute)
df['label'] = df.groupby('month')['fwd_ret_3m'].transform(
    lambda x: (x >= x.quantile(0.8)).astype(int)
)

features = ['ret_1m','ret_3m','ret_6m','ret_12m','volatility','volume_trend_3m',
            'num_funds_holding','net_fund_adds','net_shares_change',
            'fund_value_change_pct','avg_net_asset_pct']

df = df.dropna(subset=features + ['label'])
df = df.sort_values('month')
X, y = df[features], df['label']

tscv = TimeSeriesSplit(n_splits=5)
aucs = []
for train_idx, test_idx in tscv.split(X):
    model = LGBMClassifier(n_estimators=300, max_depth=5, learning_rate=0.05)
    model.fit(X.iloc[train_idx], y.iloc[train_idx])
    preds = model.predict_proba(X.iloc[test_idx])[:,1]
    aucs.append(roc_auc_score(y.iloc[test_idx], preds))

print("AUC per fold:", aucs)