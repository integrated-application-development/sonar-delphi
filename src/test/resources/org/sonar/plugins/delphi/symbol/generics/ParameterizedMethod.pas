unit ParameterizedMethod;

{This is a sample Delphi file.}

// Example from the Embarcadero wiki
// See: http://docwiki.embarcadero.com/RADStudio/Rio/en/Declaring_Generics#Parameterized_Methods

interface

type
  TFoo = class
    procedure Test;
    procedure CompareAndPrintResult<T>(X, Y: T);
  end;

implementation

procedure TFoo.CompareAndPrintResult<T>(X, Y: T);
var
  Comparer : IComparer<T>;
begin
  Comparer := TComparer<T>.Default;
  if Comparer.Compare(X, Y) = 0 then
    WriteLn('Both members compare as equal')
  else
    WriteLn('Members do not compare as equal');
end;

procedure TFoo.Test;
begin
  CompareAndPrintResult<String>('Hello', 'World');
  CompareAndPrintResult('Hello', 'Hello');
  CompareAndPrintResult<Integer>(20, 20);
  CompareAndPrintResult(10, 20);
end;

var
  F: TFoo;
begin
  F := TFoo.Create;
  F.Test;
  ReadLn;
  F.Free;
end.
end.