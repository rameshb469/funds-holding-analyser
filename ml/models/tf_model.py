"""TensorFlow model skeleton for stock pick regression.

This script defines a Keras model that consumes a short sequence (last 63 days of [close,volume,turnover]) and tabular features
and outputs a regression predicting forward 3-month return.
"""
import tensorflow as tf
from tensorflow.keras import layers, Model

SEQ_LEN = 63
F_SEQ = 3
F_TAB = 16  # placeholder, depends on engineered features


def build_model(seq_len=SEQ_LEN, f_seq=F_SEQ, f_tab=F_TAB):
    seq_input = layers.Input(shape=(seq_len, f_seq), name='seq_input')
    tab_input = layers.Input(shape=(f_tab,), name='tab_input')

    # Sequence encoder: small Transformer-like stack via conv + global pooling or LSTM
    x = layers.Bidirectional(layers.LSTM(64, return_sequences=False))(seq_input)
    x = layers.Dense(64, activation='relu')(x)
    x = layers.Dropout(0.2)(x)

    t = layers.Dense(128, activation='relu')(tab_input)
    t = layers.Dropout(0.2)(t)

    h = layers.concatenate([x, t])
    h = layers.Dense(128, activation='relu')(h)
    h = layers.Dropout(0.2)(h)
    h = layers.Dense(64, activation='relu')(h)

    out = layers.Dense(1, activation='linear', name='forward_ret')(h)

    model = Model(inputs=[seq_input, tab_input], outputs=out)
    model.compile(optimizer=tf.keras.optimizers.Adam(learning_rate=1e-4), loss='huber', metrics=['mae'])
    return model


if __name__ == '__main__':
    m = build_model()
    m.summary()

