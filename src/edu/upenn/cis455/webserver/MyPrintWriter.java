package edu.upenn.cis455.webserver;

import java.io.PrintWriter;

public class MyPrintWriter extends PrintWriter {
    public ResponseWriter writer;
    public MyServletResponse response;
    
    public MyPrintWriter(ResponseWriter buf,  MyServletResponse res) {
        super(System.out, true);
        this.writer = buf;
        this.response = res;
    }
    
    public void print(String str) {
        if(response.isCommitted()){
            throw new IllegalStateException();
        }
        else {
            writer.append(str);
        }
    }
    
    public void print(boolean b) {
        if(response.isCommitted()){
            throw new IllegalStateException();
        }
        else {
            writer.append(b);
        }
    }
    
    public void print(char c) {
        if(response.isCommitted()){
            throw new IllegalStateException();
        }
        else {
            writer.append(c);
        }
    }
    
    public void print(int i) {
        if(response.isCommitted()){
            throw new IllegalStateException();
        }
        else {
            writer.append(i);
        }
    }
    
    public void print(long l) {
        if(response.isCommitted()){
            throw new IllegalStateException();
        }
        else {
            writer.append(l);
        }
    }
    
    public void print(float f) {
        if(response.isCommitted()){
            throw new IllegalStateException();
        }
        else {
            writer.append(f);
        }
    }
    
    public void print(double d) {
        if(response.isCommitted()){
            throw new IllegalStateException();
        }
        else {
            writer.append(d);
        }
    }
    
    public void print(char[] s) {
        if(response.isCommitted()){
            throw new IllegalStateException();
        }
        else {
            writer.append(s);
        }
    }
    
    public void println(String str) {
        if(response.isCommitted()){
            throw new IllegalStateException();
        }
        else {
            writer.append(str);
            writer.append("\n");
        }
    }
    
    public void println(boolean b) {
        if(response.isCommitted()){
            throw new IllegalStateException();
        }
        else {
            writer.append(b);
            writer.append("\n");
        }
    }
    
    public void println(char c) {
        if(response.isCommitted()){
            throw new IllegalStateException();
        }
        else {
            writer.append(c);
            writer.append("\n");
        }
    }
    
    public void println(int i) {
        if(response.isCommitted()){
            throw new IllegalStateException();
        }
        else {
            writer.append(i);
            writer.append("\n");
        }
    }
    
    public void println(long l) {
        if(response.isCommitted()){
            throw new IllegalStateException();
        }
        else {
            writer.append(l);
            writer.append("\n");
        }
    }
    
    public void println(float f) {
        if(response.isCommitted()){
            throw new IllegalStateException();
        }
        else {
            writer.append(f);
            writer.append("\n");
        }
    }
    
    public void println(double d) {
        if(response.isCommitted()){
            throw new IllegalStateException();
        }
        else {
            writer.append(d);
            writer.append("\n");
        }
    }
    
    public void println(char[] s) {
        if(response.isCommitted()){
            throw new IllegalStateException();
        }
        else {
            writer.append(s);
            writer.append("\n");
        }
    }    
}