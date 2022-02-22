package com.lukeapps.basest;
import java.util.Arrays;

public class Encode {
    public int nearest_length(int input_length, int input_ratio) {
        int overlap = input_length % input_ratio;
        if (overlap == 0) {
            return input_length;
        } else {
            return ((((input_length - overlap) / input_ratio) + 1) * input_ratio);
        }
    }

    public int[] encode_raw(int input_base, int output_base, int input_ratio, int output_ratio, int[] input_data) {
        int[] input_workon = input_data.clone();
        int input_length = input_workon.length;

        if (input_base < output_base && input_length % input_ratio != 0) {
            throw new IllegalArgumentException("Input data length must be exact multiple of input ratio when output base is larger than input base");
        }
        int input_nearest_length = nearest_length(input_length, input_ratio);
        int padding_length = (input_nearest_length - input_length);
        int output_length = (input_nearest_length / input_ratio) * output_ratio;
        int[] output_data = new int[output_length];
        input_workon = Arrays.copyOf(input_workon, input_workon.length + padding_length);

        for (int i = 0; i < input_nearest_length; i += input_ratio) {
            int store = 0;
            for (int j = 0; j < input_ratio; j++) {
                int symbol = input_workon[i + j];
                symbol *= (Math.pow((double) input_base, (double) (input_ratio - j - 1)));
                store += symbol;
            }
            for (int k = 0; k < output_ratio; k++) {
                int index = ((i / input_ratio) * output_ratio) + k;
                int symbol = (int) ((int) store / Math.pow((double) output_base, (double)(output_ratio - k - 1)));
                output_data[index] = symbol;
                store -= ((int)symbol * (int)Math.pow((double)output_base, (double)(output_ratio - k - 1)));

            }
        }

        for(int i = output_length-padding_length; i < output_length; i++){
            output_data[i] = output_base;
        }
        return output_data;

    }
}
