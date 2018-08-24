unit ComplexArray;

interface

type
  TMyComplexArray = array[Low(Word)-1..(high(Word)+1)-1] of Byte;
  TMyComplexArray2 = array[0..(high(Integer))-1] of Byte;
  TMyComplexArray3 = array[0..(high(Integer) div sizeof(Integer))-1] of Integer;


implementation

end.
