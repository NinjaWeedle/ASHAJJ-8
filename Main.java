/**
 * The main loop of the interpreter.
 * @version 11/18/2021
 */
import java.io.*;
import java.util.Scanner;
import java.util.Arrays;
public class Main
{
    public static void main(String[] args)
    {
        //Initialize the values
        int pc = 512;
        int i = 0;
        int x;
        int y;
        //Get the ROM in hex
        //System.out.println("Paste Hex-converted CHIP8 rom now:");
        //String rominput = Keyboard.getString();
        int[] memory = new int[4096];
        int[] display = new int[2048];
        int[] registers = new int[16];
        int[] stack = new int[16];
        int opone;
        int optwo;
        int delaytimer = 0;
        int soundtimer = 0;
        int sp = 0;
        //file i/o shit
        if (args.length < 1) {
            System.out.println("Please provide CHIP-8 ROM!");
            System.exit(0);
        }
 
        String inputFile = args[0];
 
        try (
            InputStream inputStream = new FileInputStream(inputFile);
        ) {
            long fileSize = new File(inputFile).length();
            byte[] allBytes = new byte[(int) fileSize];
 
            int bytesRead = inputStream.read(allBytes);
 
            for(int rk = 0; rk < memory.length; rk++) {
                memory[rk + 512] = (allBytes[rk] & 0xFF);
             }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        //Start the main loop
        while (true)
        {
            //Test display
            display[0] = 1;
            display[2] = 1;
            display[64] = 1;
            display[2047] = 1;
            //Test opcodes
            //memory[512] = 0;
            //memory[513] = 224;
            //memory[514] = 0;
            //memory[515] = 253;
            //CPU
            for (int cycles = 1; cycles <= 32; cycles++)
            {
            opone = memory[pc];
            optwo = memory[pc+1];  //fetch opcodes from memory
            pc += 2;
            //Begin Instruction decoding
            if (opone == 0) //00xx range
            {
                if (optwo == 224) //00E0 Clear Screen
                {
                    Arrays.fill(display, 0);
                    continue; //Escape the cycles and draw now
                }
                if (optwo == 238) //00EE Return from Subroutine
                {
                    pc = stack[sp];
                    sp--;
                    continue;
                }
                if (optwo == 253) //00FD Exit
                {
                    System.exit(0);
                }
            }
            if (opone <= 32) //1NNN Jump
            {
                pc = optwo + (256 * (opone % 16));
                continue;
            }
            if (opone <= 48) //2NNN Call Subroutine
            {
                sp++;
                stack[sp] = pc;
                pc = optwo + (256 * (opone % 16));
                continue;
            }
            if (opone <= 64) //3XNN Skip next instruction if VX == NN
            {
                x = opone % 16;
                if (registers[x] == optwo)
                {
                    if( (memory[pc] == 1) || ((239 < memory[pc]) && (memory[pc+1] == 0))) //Skip next instruction if VX == NN
                    {
                        pc += 2;
                    }
                    pc += 2;
                }
                continue;
            }
            if (opone <= 80) //4XNN Skip next instruction if VX != NN
            {
                x = opone % 16;
                if (registers[x] != optwo)
                {
                    if( (memory[pc] == 1) || ((239 < memory[pc]) && (memory[pc+1] == 0))) //Skip next instruction if VX == NN
                    {
                        pc += 2;
                    }
                    pc += 2;
                }
                continue;
            }
            if (opone <= 96) //5XY0 Skip next instruction if VX == VY
            {
                x = opone % 16;
                y = optwo / 16;
                if (registers[x] == registers[y])
                {
                    pc += 2;
                }
                continue;
            }
            
        }
        //Tick timers
        if (delaytimer >= 1)
        {
            delaytimer--;
        }
        if (soundtimer >= 1)
        {
            soundtimer--;
        }        
        //Display
                    System.out.println();
                    int dummy = 0;
                    for (int height = 1; height <= 32; height++)
                    {
                        for (int width = 1; width <= 64; width++)
                        {
                            System.out.print(display[dummy]); //display 1 pixel from the screen buffer
                            dummy++;
                        }
                        System.out.println();
                    }
        }
    }
    
    
    
    
}
