import os
import tempfile
import pandas as pd
from ml.backtest import backtester


def make_window_csv(path, rows):
    df = pd.DataFrame(rows)
    df.to_csv(path, index=False)


def test_compute_top_from_windows_and_backtest():
    with tempfile.TemporaryDirectory() as tmp:
        # create a windows CSV with three stocks and different MKT_CAP
        csv_path = os.path.join(tmp, 'windows_test.csv')
        rows = [
            {'stock_id': 1, 'symbol': 'AAA', 'name_of_company': 'A Co', 'MKT_CAP': 100.0, 'median_vol_63': 50.0, 'has_forward': True, 'forward_ret_3m': 0.05, 'window_start': '2025-01-01', 'window_end': '2025-03-31'},
            {'stock_id': 2, 'symbol': 'BBB', 'name_of_company': 'B Co', 'MKT_CAP': 500.0, 'median_vol_63': 70.0, 'has_forward': True, 'forward_ret_3m': 0.02, 'window_start': '2025-01-01', 'window_end': '2025-03-31'},
            {'stock_id': 3, 'symbol': 'CCC', 'name_of_company': 'C Co', 'MKT_CAP': 300.0, 'median_vol_63': 30.0, 'has_forward': False, 'forward_ret_3m': None, 'window_start': '2025-01-01', 'window_end': '2025-03-31'}
        ]
        make_window_csv(csv_path, rows)

        # compute top-2 from windows
        top_set = backtester._compute_top_from_windows([csv_path], top_n=2)
        assert top_set is not None
        assert 2 in top_set and 3 in top_set or 2 in top_set and 1 in top_set

        # run backtest on the CSV window; expect picks to contain MKT_CAP field
        results = backtester.run_backtest([csv_path], n=2, score_col='median_vol_63', transaction_cost_rate=0.001, top_n=2)
        assert isinstance(results, list)
        assert len(results) == 1
        win = results[0]
        assert 'picks' in win
        for p in win['picks']:
            # picks produced by backtester should include MKT_CAP key
            assert 'MKT_CAP' in p


