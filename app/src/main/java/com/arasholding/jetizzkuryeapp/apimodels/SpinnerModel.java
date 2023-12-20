package com.arasholding.jetizzkuryeapp.apimodels;

import java.io.Serializable;

    public class SpinnerModel implements Serializable {

        private int Value;
        private String Text;

        public SpinnerModel() {
        }

        public String getText() {
            return Text;
        }

        public void setText(String text) {
            Text = text;
        }

        public SpinnerModel(int Value, String Text) {
            this.Value = Value;
            this.Text = Text;
        }

        public SpinnerModel(String Text) {
            this.Text = Text;
        }

        public int getValue() {
            return Value;
        }

        public void setValue(int id) {
            this.Value = id;
        }


        @Override
        public String toString() {
            return "Model{" +
                    "Value=" + Value +
                    ", Text='" + Text + '\'' +
                    '}';
        }
    }

