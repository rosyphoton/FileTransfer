# %% Imports
import struct
import numpy as np
from scipy.io import wavfile


def save_wav(file_name, audio, sampling_rate):
    audio = (audio * 32767).astype("int16") # 32767 is the max value of int16
    wavfile.write(file_name, sampling_rate, audio)


def fsk_encode(symbols, frequency0, frequency1, symbol_duration=1,
               Fs=44100):
    """Turns a given bit array (array of zeros and ones) into a sine wave
    using frequency shift keying.

    Parameters
    ----------
    symbols: list[int]
    frequency0: double
        The frequency of a 0 bit
    frequency1: double
        The frequency of a 1 bit
    symbol_duration: double
        The duration of each encoded symbol in seconds. If you set this to 1,
        each symbol will be played for one second in the output.
    Fs: int
        The sampling rate for the sine wave in Hz
    """
    silence = np.zeros(int(Fs / 100))  # add 10ms of silence to beginning

    # synthesize sine waves for each symbol
    # build x-axis
    total_duration = symbol_duration * len(symbols)
    x = np.linspace(0.0, total_duration, int(
        total_duration * Fs), endpoint=False)

    # calculate y-axis
    freq_symbols = [frequency0 if s == 0 else frequency1 for s in symbols]
    f = np.repeat(freq_symbols, int(symbol_duration * Fs))
    waves = np.sin(2 * np.pi * f * x)

    return np.concatenate((silence, waves, silence), axis=0)


def gen_fsk_audio(input, filepath, frequency0=30, frequency1=40, symbol_duration=1, sampling_rate=8000):
    # turn input string into binary list
    symbols = list(
        map(int, (''.join('{0:08b}'.format(ord(x), 'b') for x in input))))
    filename = filepath + input + ".wav"
    save_wav(filename, fsk_encode(symbols, frequency0, frequency1,
                                  symbol_duration, sampling_rate), sampling_rate)


def vibration_encode(symbols, f, symbol_duration=0.5, duty0=0.2, duty1=0.8, Fs=44100):
    L = round(symbol_duration * Fs)

    t_symbol = np.arange(L) / Fs
    t_one = np.arange(L * duty1) / Fs
    t_zero = np.arange(L * duty0) / Fs

    silence_symbol = list(np.zeros(L))
    scream_symbol = list(np.sin(2 * np.pi * f * t_symbol))
    one_symbol = list(np.sin(2 * np.pi * f * t_one)) + \
        list(np.zeros(len(t_zero)))
    zero_symbol = list(np.sin(2 * np.pi * f * t_zero)) + \
        list(np.zeros(len(t_one)))

    preamble = scream_symbol * 5 + silence_symbol

    encoded_symbols = [one_symbol if s else zero_symbol for s in symbols]
    encoded_symbols = [item for sublist in encoded_symbols for item in sublist]

    signal = silence_symbol * 2 + preamble + encoded_symbols + silence_symbol * 2

    return np.array(signal)


def gen_motion_sensor_audio(input, filepath, frequency=86, symbol_duration=1.5, sampling_rate=8000):
    # turn input string into binary list
    symbols = list(
        map(int, (''.join('{0:08b}'.format(ord(x), 'b') for x in input))))
    filename = filepath + input + "_vib.wav"
    save_wav(filename, vibration_encode(symbols, frequency,
                                        symbol_duration, 0.2, 0.8, sampling_rate), sampling_rate)
