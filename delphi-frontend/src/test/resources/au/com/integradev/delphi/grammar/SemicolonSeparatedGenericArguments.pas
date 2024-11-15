unit SemicolonSeparatedGenericArguments;

interface

implementation

type
  TFoo = class
    class procedure Bar<T; C>;
  end;

class procedure TFoo.Bar<T; C>;
begin
  // ...
end;

end.