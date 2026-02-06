Stock-picking ML pipeline

This folder contains helper scripts to extract data, build monthly windows, compute features, train simple models and run a backtest.

Quick start (Python virtualenv recommended):

python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt

Files added:
- sql/queries.sql    -- parameterized SQL snippets
- preprocess/load_data.py -- fetch top-N and download raw daily history (DB connector required)
- preprocess/window_builder.py -- build month-end windows and feature aggregates
- backtest/backtester.py -- monthly pick backtester implementing 0.1% transaction costs
- models/tf_model.py -- TensorFlow model skeleton

This is a minimal bootstrap. Configure DB connection params via environment or update the scripts' constants. The scripts are written to be self-contained and easy to extend.

