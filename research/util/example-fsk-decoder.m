%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
% Signal 5 Decoder
%
% Exercise 3.5, PhySec winter term 2018/19
% Author: Johannes Lauinger
% Created: 13.12.2018
%
% Usage: decode_signal5(filename)
%   filename is the path to signal 5
%
% Output format: Prints flags
%
% Required packages:
%   octave-signal 1.4.0-1
%
% Tested with Octave 4.4.1-3 on ArchLinux
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%


function decode_signal5(filename)
    [I, Q] = read_data(filename);
    symbols = extract_symbols(I);
    flag = decode_symbols(symbols);
    disp(flag)
end

function [I, Q] = read_data(filename)
    % read the raw file data
    fid = fopen(filename, 'r');
    data = fread(fid);
    fclose(fid);
    
    % read interleaved I/Q samples and scale uint8 to a floating point number
    I = (data(1:2:end)-128)/255;
    Q = (data(2:2:end)-128)/255;
end

function symbols = extract_symbols(soi)
    symbol_length = 128000; % samples (magic number)
    fs = 2e6; % Hz
    
    symbols = [];
    for index = symbol_length/4:symbol_length:length(soi)
        % for every symbol of symbol_length samples, we take the samples from
        % 25% until 75% to avoid possible weird effects near the border of the
        % symbol
        samples = soi(index:index+symbol_length/2);
        % extract the predominant frequency in this samples region
        fc = extract_carrier_frequency(samples, fs);
        % add the extracted frequency directly as the extracted symbol
        symbols = [symbols fc];
    end
end

function fc = extract_carrier_frequency(x, fs)
    % calculate Fourier transform of wave data using FFT
    Y = fft(x);
    L = length(x);

    % compute one-sided spectrogram
    % see: https://de.mathworks.com/help/matlab/ref/fft.html
    P2 = abs(Y/L);
    P1 = P2(1:L/2+1);
    P1(2:end-1) = 2*P1(2:end-1);
    f = fs*(0:(L/2))/L;

    % find highest peak in frequency domain and its index
    [pks, loc, extra] = findpeaks(P1);
    [m,i] = max(pks);

    % take frequency at that index as estimated carrier frequency
    fc = f(loc(i));
end

function text = decode_symbols(symbols)
    % ASCII:
    %  # 035
    %  S 083
    %  E 069
    %  M 077
    %  O 079
    
    % calculate the relative ratios between known symbols to calibrate the
    % decoding process
    calibration_factors = [ (symbols(2)/symbols(1)) / (83/35),   % S to #
                            (symbols(3)/symbols(1)) / (69/35),   % E to #
                            (symbols(5)/symbols(1)) / (77/35),   % M to #
                            (symbols(6)/symbols(1)) / (79/35),   % O to #
                            (symbols(5)/symbols(2)) / (77/83) ]; % M to S

    % use the mean ratio to predict the characters at untrained frequencies
    ratio = mean(calibration_factors);
    frequency_of_hash = symbols(1);
    
    % start with a vector of one space to avoid Octave complaining about
    % an implicit conversion between numeric / character vector
    text = [' '];
    for symbol = symbols
        % for every symbol, use the predictor model to estimate the ASCII value
        % and convert to a character
        character = char(symbol/frequency_of_hash * 35);
        text = [text character];
    end
    % remove the leading space again
    text = text(2:end);
end
