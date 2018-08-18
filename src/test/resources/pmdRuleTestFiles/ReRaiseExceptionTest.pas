unit ReRaiseExceptionTest;

interface


implementation

// Non compliant
 var
   number1, number0 : Integer;
 begin
   try
     number0 := 0;
     number1 := 1;
     number1 := number1 div number0;
     ShowMessage('1 / 0 = '+IntToStr(number1));
   except
     on E : Exception do
     begin
       ShowMessage('Exception class name = '+E.ClassName);
       ShowMessage('Exception message = '+E.Message);
       raise E;
     end;
   end;
end.