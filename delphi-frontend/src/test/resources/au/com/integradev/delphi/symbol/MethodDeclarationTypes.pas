unit MethodDeclarationTypes;

interface

type
  TypeAndFunc = class(TObject)
  end;

  TBaseType = class(TObject)
    function TypeAndFunc: String;
  end;


  TOuterType = class(TObject)
    type TNestedType = class(TObject)
      function DoNestedWork(A: TypeAndFunc): TypeAndFunc;

      type TDoubleNestedType = class(TBaseType)
        function DoDoubleNestedWork(A: TypeAndFunc): TypeAndFunc;
      end;
    end;

    function TypeAndFunc: Integer;
    function DoWork(A: TypeAndFunc): TypeAndFunc;
  end;

implementation

type TypeAndFuncClass = class of TypeAndFunc;

procedure Test(A: Integer); overload;
begin
  Writeln('Func on TOuterType');
end;


procedure Test(A: TypeAndFuncClass); overload;
begin
  Writeln('Type');
end;

procedure Test(A: String); overload;
begin
  Writeln('Func on TBaseType');
end;

function TOuterType.TypeAndFunc: Integer;
begin
end;

function TBaseType.TypeAndFunc: String;
begin
end;

function TOuterType.DoWork(A: TypeAndFunc): TypeAndFunc;
begin
  Test(TypeAndFunc); // Func on TOuterType
end;

function TOuterType.TNestedType.DoNestedWork(A: TypeAndFunc): TypeAndFunc;
begin
  Test(TypeAndFunc); // Type
end;

function TOuterType.TNestedType.TDoubleNestedType.DoDoubleNestedWork(A: TypeAndFunc): TypeAndFunc;
begin
  Test(TypeAndFunc); // Func on TBaseType
end;

end.
